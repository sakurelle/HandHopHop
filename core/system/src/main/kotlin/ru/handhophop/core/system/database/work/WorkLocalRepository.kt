package ru.handhophop.core.system.database.work

import android.content.Context
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Keep chunks small so each CursorWindow row stays safe.
// 1024 chars per chunk is intentionally conservative.
private const val PROGRESS_CHUNK_SIZE = 1024
private const val WORK_IMAGES_DIRECTORY = "work_images"
private val dayFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

data class WorkLocalItem(
    val id: Long = 0,
    val url: String,
    val image: ByteArray? = null,
    val imagePath: String? = null,
    val isFavorite: Boolean = false,
    val projectName: String? = null,
    val schemeType: String? = null,
    val colorCount: Int? = null,
    val difficulty: String? = null,
    val gridWidth: Int? = null,
    val gridHeight: Int? = null,
    val gridRle: String? = null,
    val percentage: Int? = null,
    val spentTime: Long? = null,
)

class WorkLocalRepository(
    private val workDao: WorkDao,
    private val appContext: Context? = null,
) {

    suspend fun clearAllWorks() {
        deleteImageFiles(workDao.getAllImagePaths())
        workDao.deleteAllWithProgress()
    }

    fun getDatabaseSize(context: Context): Long {
        val dbPath = context.getDatabasePath("hand_hop_hop.db").absolutePath

        val files = listOf(
            File(dbPath),
            File("$dbPath-wal"),
            File("$dbPath-shm"),
        )

        return files.filter { it.exists() }.sumOf { it.length() }
    }

    suspend fun getWorkCount(): Int {
        return workDao.getCount()
    }

    suspend fun addWorkActivityTime(
        workId: Long,
        startedAtMillis: Long,
        endedAtMillis: Long,
    ) {
        if (workId <= 0L || endedAtMillis <= startedAtMillis) {
            return
        }

        splitByDay(startedAtMillis, endedAtMillis).forEach { (day, spentTimeMillis) ->
            workDao.addWorkActivityTime(
                workId = workId,
                day = day,
                spentTimeToAdd = spentTimeMillis,
            )
        }
    }

    suspend fun getWorkActivityStats(workId: Long): WorkActivityStats {
        if (workId <= 0L) {
            return WorkActivityStats()
        }

        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val monday = today.minusDays(((today.dayOfWeek.value + 6) % 7).toLong())
        val sunday = monday.plusDays(6)
        val activityDays = workDao.getActivityDays(
            workId = workId,
            startDay = monday.format(dayFormatter),
            endDay = sunday.format(dayFormatter),
        ).associateBy(WorkActivityDayEntity::day)

        val weekSpentTimeMillisByDay = (0..6).map { dayOffset ->
            val day = monday.plusDays(dayOffset.toLong()).format(dayFormatter)
            activityDays[day]?.spentTime ?: 0L
        }

        return WorkActivityStats(
            todaySpentTimeMillis = activityDays[today.format(dayFormatter)]?.spentTime ?: 0L,
            weekSpentTimeMillisByDay = weekSpentTimeMillisByDay,
        )
    }

    suspend fun addFavorite(work: WorkLocalItem): Long {
        val current = findCurrentDetails(work)
        val imagePath = persistImageIfNeeded(
            url = work.url,
            imageBytes = work.image,
            currentImagePath = current?.imagePath,
        )

        return if (current == null) {
            workDao.insert(
                work.copy(
                    isFavorite = true,
                    image = null,
                    imagePath = imagePath,
                    gridRle = null,
                ).toEntity(),
            )
        } else {
            workDao.updateFavoriteById(
                id = current.id,
                url = work.url,
                imagePath = imagePath,
            )

            current.id
        }
    }

    suspend fun removeFavorite(url: String) {
        deleteImageFiles(workDao.getImagePathsByUrl(url))
        workDao.deleteByUrlWithProgress(url)
    }

    suspend fun addWork(work: WorkLocalItem): Long {
        val current = findCurrentDetails(work)
        val progressChunks = work.gridRle.toProgressChunks()
        val imagePath = persistImageIfNeeded(
            url = work.url,
            imageBytes = work.image,
            currentImagePath = current?.imagePath,
        )

        return if (current == null) {
            val insertedId = workDao.insert(
                work.copy(
                    image = null,
                    imagePath = imagePath,
                    gridRle = null,
                ).toEntity(),
            )

            workDao.replaceProgressChunks(
                workId = insertedId,
                rleChunks = progressChunks,
            )

            insertedId
        } else {
            workDao.updateWorkById(
                id = current.id,
                url = work.url,
                imagePath = imagePath,
                projectName = work.projectName,
                schemeType = work.schemeType,
                colorCount = work.colorCount,
                difficulty = work.difficulty,
                gridWidth = work.gridWidth,
                gridHeight = work.gridHeight,
                percentage = work.percentage,
                spendedTime = work.spentTime,
            )

            workDao.replaceProgressChunks(
                workId = current.id,
                rleChunks = progressChunks,
            )

            current.id
        }
    }

    suspend fun removeWork(id: Long) {
        val current = workDao.getDetailsById(id)

        if (current?.url.isNullOrBlank()) {
            deleteImageFile(workDao.getImagePathById(id))
            workDao.deleteByIdWithProgress(id)
            return
        }

        deleteImageFiles(workDao.getImagePathsByUrl(current.url.orEmpty()))
        workDao.deleteByUrlWithProgress(current.url.orEmpty())
    }

    suspend fun getAllWorks(): List<WorkLocalItem> {
        return workDao.getAll().map(WorkUrlPreview::toLocalItem)
    }

    suspend fun getFavoriteWorks(): List<WorkLocalItem> {
        return workDao.getFavorites()
            .map(WorkFavoritePreview::toLocalItem)
            .distinctBy { it.url }
    }

    suspend fun getWorksByUrls(urls: List<String>): List<WorkLocalItem> {
        if (urls.isEmpty()) {
            return emptyList()
        }

        return workDao.getByUrls(urls).map(WorkUrlPreview::toLocalItem)
    }

    suspend fun getWorkByUrl(url: String): WorkLocalItem? {
        return workDao.getDetailsByUrl(url)?.toLocalItemWithProgress()
    }

    suspend fun getWorkById(id: Long): WorkLocalItem? {
        return workDao.getDetailsById(id)?.toLocalItemWithProgress()
    }

    /**
     * Используется при открытии работы из избранного/ленты.
     *
     * Важно:
     * - не читает image BLOB из БД;
     * - использует metadata + image_path/url;
     * - прогресс читает только из work_progress_chunk / legacy grid_rle;
     * - сначала пытается найти работу по id, потом по url;
     * - если по id пришла favorite-only запись без прогресса, а по url есть полноценная работа,
     *   возвращает полноценную работу по url.
     */
    suspend fun getWorkForOpening(
        id: Long?,
        url: String?,
    ): WorkLocalItem? {
        val byId = id
            ?.takeIf { it > 0 }
            ?.let { workDao.getDetailsById(it)?.toLocalItemWithProgress() }

        val byUrl = url
            ?.takeIf { it.isNotBlank() }
            ?.let { workDao.getDetailsByUrl(it)?.toLocalItemWithProgress() }

        return listOfNotNull(byId, byUrl)
            .maxWithOrNull(
                compareBy<WorkLocalItem> { it.openPriority() }
                    .thenBy { if (it.id == byId?.id) 1 else 0 }
                    .thenBy { it.id },
            )
    }

    suspend fun isFavorite(url: String): Boolean {
        return workDao.isFavoriteByUrl(url) == true
    }

    private suspend fun findCurrentDetails(work: WorkLocalItem): WorkDetailsPreview? {
        if (work.id > 0) {
            val byId = workDao.getDetailsById(work.id)

            if (byId != null) {
                return byId
            }
        }

        return workDao.getDetailsByUrl(work.url)
    }

    private suspend fun WorkDetailsPreview.toLocalItemWithProgress(): WorkLocalItem {
        val chunkedProgress = workDao.getProgressChunks(id)
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = "")

        val legacyProgress = if (chunkedProgress == null) {
            workDao.getLegacyGridRleById(id)
        } else {
            null
        }

        if (chunkedProgress == null && !legacyProgress.isNullOrBlank()) {
            workDao.replaceProgressChunks(
                workId = id,
                rleChunks = legacyProgress.toProgressChunks(),
            )
        }

        return toLocalItem(
            progressRle = chunkedProgress ?: legacyProgress,
        )
    }

    private fun WorkLocalItem.openPriority(): Int {
        return when {
            hasCreatedWorkConfig() && hasProgress() -> 4
            hasCreatedWorkConfig() -> 3
            hasProgress() -> 2
            hasTrackedTime() -> 1
            else -> 0
        }
    }

    private fun persistImageIfNeeded(
        url: String,
        imageBytes: ByteArray?,
        currentImagePath: String?,
    ): String? {
        if (imageBytes == null || imageBytes.isEmpty()) {
            return currentImagePath
        }

        val filesDirectory = appContext?.filesDir ?: return currentImagePath
        val workImagesDirectory = File(filesDirectory, WORK_IMAGES_DIRECTORY)
        if (!workImagesDirectory.exists()) {
            workImagesDirectory.mkdirs()
        }

        val fileName = buildString {
            append(url.hashCode().toUInt().toString(16))
            append(".png")
        }

        val imageFile = File(workImagesDirectory, fileName)

        return runCatching {
            imageFile.writeBytes(imageBytes)
            imageFile.absolutePath
        }.getOrElse {
            currentImagePath
        }
    }

    private fun deleteImageFiles(paths: List<String?>) {
        paths.filterNotNull()
            .distinct()
            .forEach(::deleteImageFile)
    }

    private fun deleteImageFile(path: String?) {
        if (path.isNullOrBlank()) {
            return
        }

        runCatching {
            File(path).takeIf(File::exists)?.delete()
        }
    }

    private fun splitByDay(
        startedAtMillis: Long,
        endedAtMillis: Long,
    ): List<Pair<String, Long>> {
        if (endedAtMillis <= startedAtMillis) {
            return emptyList()
        }

        val zoneId = ZoneId.systemDefault()
        val result = mutableListOf<Pair<String, Long>>()
        var segmentStart = Instant.ofEpochMilli(startedAtMillis).atZone(zoneId)
        val sessionEnd = Instant.ofEpochMilli(endedAtMillis).atZone(zoneId)

        while (segmentStart.toInstant().toEpochMilli() < endedAtMillis) {
            val nextDayStart = segmentStart.toLocalDate()
                .plusDays(1)
                .atStartOfDay(zoneId)
            val segmentEnd = minOf(nextDayStart, sessionEnd)
            val spentTimeMillis = segmentEnd.toInstant().toEpochMilli() -
                segmentStart.toInstant().toEpochMilli()

            if (spentTimeMillis > 0L) {
                result += segmentStart.toLocalDate().format(dayFormatter) to spentTimeMillis
            }

            segmentStart = segmentEnd
        }

        return result
    }
}

fun WorkLocalItem.hasCreatedWorkConfig(): Boolean {
    return !projectName.isNullOrBlank() &&
            !schemeType.isNullOrBlank() &&
            colorCount != null &&
            colorCount > 0 &&
            !difficulty.isNullOrBlank()
}

fun WorkLocalItem.hasCreatedWork(): Boolean {
    return hasCreatedWorkConfig()
}

fun WorkLocalItem.hasStartedWork(): Boolean {
    return hasCreatedWorkConfig() || hasProgress() || hasTrackedTime()
}

fun WorkLocalItem.hasProgress(): Boolean {
    return !gridRle.isNullOrBlank() || (percentage ?: 0) > 0
}

fun WorkLocalItem.hasTrackedTime(): Boolean {
    return (spentTime ?: 0L) > 0L
}

private fun WorkLocalItem.toEntity(): WorkEntity {
    return WorkEntity(
        id = id,
        isFavorite = isFavorite,
        projectName = projectName,
        schemeType = schemeType,
        colorCount = colorCount,
        difficulty = difficulty,
        url = url,

        // Не сохраняем картинку в БД, чтобы не ловить Row too big to fit into CursorWindow.
        // Для восстановления схемы используется image_path/url.
        image = null,
        imagePath = imagePath,

        gridWidth = gridWidth,
        gridHeight = gridHeight,

        // Новый RLE не пишем в work.grid_rle.
        // Прогресс хранится чанками в work_progress_chunk.
        gridRle = null,

        percentage = percentage,
        spendedTime = spentTime,
    )
}

private fun WorkFavoritePreview.toLocalItem(): WorkLocalItem {
    return WorkLocalItem(
        id = id,
        url = url.orEmpty(),
        imagePath = imagePath,
        image = null,

        isFavorite = true,
    )
}

private fun WorkUrlPreview.toLocalItem(): WorkLocalItem {
    return WorkLocalItem(
        id = id,
        url = url.orEmpty(),
        image = null,
        isFavorite = isFavorite,
        projectName = projectName,
        schemeType = schemeType,
        colorCount = colorCount,
        difficulty = difficulty,
        gridWidth = gridWidth,
        gridHeight = gridHeight,
        imagePath = imagePath,
        percentage = percentage,
        spentTime = spendedTime,
    )
}

private fun WorkDetailsPreview.toLocalItem(
    progressRle: String?,
): WorkLocalItem {
    return WorkLocalItem(
        id = id,
        url = url.orEmpty(),
        image = null,
        isFavorite = isFavorite,
        projectName = projectName,
        schemeType = schemeType,
        colorCount = colorCount,
        difficulty = difficulty,
        gridWidth = gridWidth,
        gridHeight = gridHeight,
        imagePath = imagePath,
        gridRle = progressRle,
        percentage = percentage,
        spentTime = spendedTime,
    )
}

private fun String?.toProgressChunks(): List<String> {
    if (isNullOrEmpty()) {
        return emptyList()
    }

    return chunked(PROGRESS_CHUNK_SIZE)
}
