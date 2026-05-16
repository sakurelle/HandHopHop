package ru.handhophop.core.system.database.work

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
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
                    gridRle = null,
                ).toEntity(),
            )
        } else {
            workDao.updateFavoriteById(
                id = current.id,
                url = work.url,
                image = work.image,
            )
            current.id
        }
    }

    suspend fun addWork(work: WorkLocalItem): Long {
        val current = findCurrentDetails(work)
        val progressChunks = work.gridRle.toProgressChunks()

        return if (current == null) {
            val insertedId = workDao.insert(
                work.copy(gridRle = null).toEntity(),
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
                image = work.image,
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

    suspend fun removeFavorite(url: String) {
        workDao.deleteByUrlWithProgress(url)
    }

    suspend fun getAllWorks(): List<WorkLocalItem> {
        return workDao.getAll().map(WorkUrlPreview::toLocalItem)
    }

    suspend fun getFavoriteWorks(): List<WorkLocalItem> {
        return workDao.getFavorites().map(WorkFavoritePreview::toLocalItem)
    }

    suspend fun getWorksByUrls(urls: List<String>): List<WorkLocalItem> {
        if (urls.isEmpty()) return emptyList()

        return workDao.getByUrls(urls).map(WorkUrlPreview::toLocalItem)
    }

    suspend fun getWorkByUrl(url: String): WorkLocalItem? {
        val details = workDao.getDetailsByUrl(url) ?: return null
        return details.toLocalItemWithProgress()
    }

    suspend fun getWorkById(id: Long): WorkLocalItem? {
        val details = workDao.getDetailsById(id) ?: return null
        return details.toLocalItemWithProgress()
    }

    suspend fun isFavorite(url: String): Boolean {
        return workDao.isFavoriteByUrl(url) == true
    }

    private suspend fun findCurrentDetails(work: WorkLocalItem): WorkDetailsPreview? {
        return if (work.id > 0) {
            workDao.getDetailsById(work.id)
        } else {
            workDao.getDetailsByUrl(work.url)
        }
    }

    private suspend fun WorkDetailsPreview.toLocalItemWithProgress(): WorkLocalItem {
        val image = workDao.getImageById(id)

        val chunkedProgress = workDao.getProgressChunks(id)
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = "")

        val progressRle = if (chunkedProgress != null) {
            chunkedProgress
        } else {
            val legacyRle = workDao.getLegacyGridRleById(id)

            if (!legacyRle.isNullOrBlank()) {
                workDao.replaceProgressChunks(
                    workId = id,
                    rleChunks = legacyRle.toProgressChunks(),
                )
            }

            legacyRle
        }

        return WorkLocalItem(
            id = id,
            url = url.orEmpty(),
            image = image,
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
}

fun WorkLocalItem.hasCreatedWork(): Boolean {
    return !projectName.isNullOrBlank() &&
            !schemeType.isNullOrBlank() &&
            colorCount != null &&
            colorCount > 0 &&
            !difficulty.isNullOrBlank()
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

fun WorkLocalItem.hasStartedWork(): Boolean {
    return hasCreatedWorkConfig() ||
            !gridRle.isNullOrBlank() ||
            (percentage ?: 0) > 0 ||
            (spentTime ?: 0L) > 0L
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
        image = image,
        gridWidth = gridWidth,
        gridHeight = gridHeight,
        gridRle = null,
        percentage = percentage,
        spendedTime = spentTime,
    )
}

private fun WorkFavoritePreview.toLocalItem(): WorkLocalItem {
    return WorkLocalItem(
        id = id,
        url = url.orEmpty(),
        image = image,
        isFavorite = true,
    )
}

private fun WorkUrlPreview.toLocalItem(): WorkLocalItem {
    return WorkLocalItem(
        id = id,
        url = url.orEmpty(),
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

private fun String?.toProgressChunks(): List<String> {
    if (isNullOrEmpty()) return emptyList()

    return chunked(PROGRESS_CHUNK_SIZE)
}