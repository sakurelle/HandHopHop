package ru.handhophop.core.system.database.work

import android.content.Context
import java.io.File

private const val PROGRESS_CHUNK_SIZE = 64 * 1024

data class WorkLocalItem(
    val id: Long = 0,
    val url: String,
    val image: ByteArray? = null,
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
) {

    suspend fun clearAllWorks() {
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

    suspend fun addFavorite(work: WorkLocalItem): Long {
        val current = findCurrentDetails(work)

        return if (current == null) {
            workDao.insert(
                work.copy(
                    isFavorite = true,
                    image = null,
                    gridRle = null,
                ).toEntity(),
            )
        } else {
            workDao.updateFavoriteById(
                id = current.id,
                url = work.url,
                image = null,
            )

            current.id
        }
    }

    suspend fun removeFavorite(url: String) {
        workDao.deleteByUrlWithProgress(url)
    }

    suspend fun addWork(work: WorkLocalItem): Long {
        val current = findCurrentDetails(work)
        val progressChunks = work.gridRle.toProgressChunks()

        return if (current == null) {
            val insertedId = workDao.insert(
                work.copy(
                    image = null,
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
                image = null,
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
        workDao.deleteByIdWithProgress(id)
    }

    suspend fun getAllWorks(): List<WorkLocalItem> {
        return workDao.getAll().map(WorkUrlPreview::toLocalItem)
    }

    suspend fun getFavoriteWorks(): List<WorkLocalItem> {
        return workDao.getFavorites().map(WorkFavoritePreview::toLocalItem)
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
     * - не читает image из БД;
     * - не вызывает getImageById();
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

        return when {
            byUrl?.hasCreatedWorkConfig() == true && byUrl.hasProgress() -> byUrl
            byId?.hasCreatedWorkConfig() == true && byId.hasProgress() -> byId
            byUrl?.hasCreatedWorkConfig() == true -> byUrl
            byId?.hasCreatedWorkConfig() == true -> byId
            byId != null -> byId
            else -> byUrl
        }
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
        // Для восстановления схемы используется url.
        image = null,

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

        // Для экрана избранного image не читаем.
        // UI должен загрузить превью по url.
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