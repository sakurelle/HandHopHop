package ru.handhophop.core.system.database.work

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val PROGRESS_CHUNK_SIZE = 1024
private const val WORK_IMAGES_DIRECTORY = "work_images"

@RequiresApi(Build.VERSION_CODES.O)
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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WorkLocalItem

        if (id != other.id) return false
        if (isFavorite != other.isFavorite) return false
        if (colorCount != other.colorCount) return false
        if (gridWidth != other.gridWidth) return false
        if (gridHeight != other.gridHeight) return false
        if (percentage != other.percentage) return false
        if (spentTime != other.spentTime) return false
        if (url != other.url) return false
        if (!image.contentEquals(other.image)) return false
        if (imagePath != other.imagePath) return false
        if (projectName != other.projectName) return false
        if (schemeType != other.schemeType) return false
        if (difficulty != other.difficulty) return false
        if (gridRle != other.gridRle) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + (colorCount ?: 0)
        result = 31 * result + (gridWidth ?: 0)
        result = 31 * result + (gridHeight ?: 0)
        result = 31 * result + (percentage ?: 0)
        result = 31 * result + (spentTime?.hashCode() ?: 0)
        result = 31 * result + url.hashCode()
        result = 31 * result + (image?.contentHashCode() ?: 0)
        result = 31 * result + (imagePath?.hashCode() ?: 0)
        result = 31 * result + (projectName?.hashCode() ?: 0)
        result = 31 * result + (schemeType?.hashCode() ?: 0)
        result = 31 * result + (difficulty?.hashCode() ?: 0)
        result = 31 * result + (gridRle?.hashCode() ?: 0)
        return result
    }
}

class WorkLocalRepository(
    private val workDao: WorkDao,
    private val appContext: Context? = null,
) {
    companion object {
        private val workDataVersion = MutableStateFlow(0)
    }

    fun observeWorkDataVersion(): StateFlow<Int> = workDataVersion.asStateFlow()

    private fun notifyWorkDataChanged() {
        workDataVersion.value += 1
    }

    suspend fun clearAllWorks() {
        deleteImageFiles(workDao.getAllImagePaths())
        workDao.deleteAllWithProgress()
        notifyWorkDataChanged()
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

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
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
        val imagePath = work.imagePath
            ?: persistImageIfNeeded(
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
        }.also {
            notifyWorkDataChanged()
        }
    }

    suspend fun removeFavorite(url: String) {
        workDao.clearFavoriteByUrl(url)
        notifyWorkDataChanged()
    }

    suspend fun addWork(work: WorkLocalItem): Long {
        val current = findCurrentDetails(work)
        val progressChunks = work.gridRle.toProgressChunks()
        val imagePath = work.imagePath
            ?: persistImageIfNeeded(
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
                isFavorite = work.isFavorite,
                projectName = work.projectName,
                schemeType = work.schemeType,
                colorCount = work.colorCount,
                difficulty = work.difficulty,
                gridWidth = work.gridWidth,
                gridHeight = work.gridHeight,
                percentage = work.percentage,
                spentTime = work.spentTime
            )

            workDao.replaceProgressChunks(
                workId = current.id,
                rleChunks = progressChunks,
            )

            current.id
        }.also {
            notifyWorkDataChanged()
        }
    }

    suspend fun removeWork(id: Long) {
        val current = workDao.getDetailsById(id)

        if (current?.url.isNullOrBlank()) {
            deleteImageFile(workDao.getImagePathById(id))
            workDao.deleteByIdWithProgress(id)
            notifyWorkDataChanged()
            return
        }

        deleteImageFiles(workDao.getImagePathsByUrl(current.url))
        workDao.deleteByUrlWithProgress(current.url)
        notifyWorkDataChanged()
    }

    suspend fun getAllWorks(): List<WorkLocalItem> {
        return workDao.getAll().map(WorkUrlPreview::toLocalItem)
    }

    suspend fun getFeedMetaByUrls(urls: List<String>): List<WorkFeedMeta> {
        if (urls.isEmpty()) {
            return emptyList()
        }

        return workDao.getFeedMetaByUrls(urls)
    }

    suspend fun getBookmarkPreviews(): List<WorkBookmarkPreview> {
        return workDao.getBookmarkPreviews()
    }

    suspend fun getWorkById(id: Long): WorkLocalItem? {
        return workDao.getDetailsById(id)?.toLocalItemWithProgress()
    }

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

        return toLocalItem( progressRle = chunkedProgress )
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

    @RequiresApi(Build.VERSION_CODES.O)
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
        image = null,
        imagePath = imagePath,
        gridWidth = gridWidth,
        gridHeight = gridHeight,
        percentage = percentage,
        spentTime = spentTime,
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
        spentTime = spentTime,
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
        spentTime = spentTime,
    )
}

private fun String?.toProgressChunks(): List<String> {
    if (isNullOrEmpty()) {
        return emptyList()
    }

    return chunked(PROGRESS_CHUNK_SIZE)
}
