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
        result = 31 * result + (projectName?.hashCode() ?: 0)
        result = 31 * result + (schemeType?.hashCode() ?: 0)
        result = 31 * result + (difficulty?.hashCode() ?: 0)
        result = 31 * result + (gridRle?.hashCode() ?: 0)
        return result
    }
}

class WorkLocalRepository(
    private val workDao: WorkDao,
) {

    suspend fun clearAllWorks() {
        workDao.deleteAllWithProgress()
        workDao.checkpoint(SimpleSQLiteQuery("VACUUM"))
    }


    fun getDatabaseSize(context: Context): Long {
        val dbPath = context.getDatabasePath("hand_hop_hop.db").absolutePath
        val files = listOf(
            File(dbPath),
            File("$dbPath-wal"),
            File("$dbPath-shm")
        )
        return files.filter { it.exists() }.sumOf { it.length() }
    }

    suspend fun getWorkCount(): Int {
        return workDao.getCount()
    }


    suspend fun addFavorite(work: WorkLocalItem): Long {
        return upsert(work) { current ->
            current.copy(
                url = work.url,
                image = work.image ?: current.image,
                isFavorite = true,
            )
        }
    }

    suspend fun removeFavorite(url: String) {
        workDao.deleteByUrlWithProgress(url)
    }

    suspend fun addWork(work: WorkLocalItem): Long {
        return upsert(
            work = work.copy(gridRle = null),
            progressRle = work.gridRle,
            replaceProgress = work.gridRle != null,
        ) { current ->
            current.copy(
                url = work.url,
                image = work.image ?: current.image,
                projectName = work.projectName,
                schemeType = work.schemeType,
                colorCount = work.colorCount,
                difficulty = work.difficulty,
                gridWidth = work.gridWidth,
                gridHeight = work.gridHeight,
                gridRle = null,
                percentage = work.percentage,
                spendedTime = work.spentTime,
            )
        }
    }

    suspend fun removeWork(id: Long) {
        workDao.deleteByIdWithProgress(id)
    }

    suspend fun getAllWorks(): List<WorkLocalItem> {
        return workDao.getAll().map(WorkDetailsPreview::toLocalItem)
    }

    suspend fun getFavoriteWorks(): List<WorkLocalItem> {
        return workDao.getFavorites().map(WorkFavoritePreview::toLocalItem)
    }

    suspend fun getWorkByUrl(url: String): WorkLocalItem? {
        return workDao.getDetailsByUrl(url)?.toLocalItemWithProgress()
    }

    suspend fun isFavorite(url: String): Boolean {
        return workDao.isFavoriteByUrl(url) == true
    }

    suspend fun getWorksByUrls(urls: List<String>): List<WorkLocalItem> {
        if (urls.isEmpty()) return emptyList()
        return workDao.getByUrls(urls).map(WorkUrlPreview::toLocalItem)
    }

    suspend fun getWorkById(id: Long): WorkLocalItem? {
        return workDao.getDetailsById(id)?.toLocalItemWithProgress()
    }

    private suspend fun upsert(
        work: WorkLocalItem,
        progressRle: String? = null,
        replaceProgress: Boolean = false,
        updateCurrent: (WorkEntity) -> WorkEntity,
    ): Long {
        val currentDetails = when {
            work.id > 0 -> workDao.getDetailsById(work.id)
            else -> workDao.getDetailsByUrl(work.url)
        }
        val current = currentDetails?.toEntity(
            image = workDao.getImageById(currentDetails.id),
        )

        val progressChunks = progressRle.toProgressChunks()

        return if (current == null) {
            if (replaceProgress) {
                workDao.insertWithProgress(
                    work = work.toEntity(),
                    rleChunks = progressChunks,
                )
            } else {
                workDao.insert(work.toEntity())
            }
        } else {
            if (replaceProgress) {
                workDao.updateWithProgress(
                    work = updateCurrent(current),
                    rleChunks = progressChunks,
                )
            } else {
                workDao.update(updateCurrent(current))
            }
            current.id
        }
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

        val progressRle = chunkedProgress ?: legacyProgress

        if (chunkedProgress == null && !legacyProgress.isNullOrBlank()) {
            workDao.replaceProgressChunks(id, legacyProgress.toProgressChunks())
            workDao.clearLegacyProgress(id)
        }

        return toLocalItem(
            image = workDao.getImageById(id),
            progressRle = progressRle,
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
        image = image,
        gridWidth = gridWidth,
        gridHeight = gridHeight,
        gridRle = gridRle,
        percentage = percentage,
        spendedTime = spentTime,
    )
}

private fun WorkDetailsPreview.toLocalItem(
    image: ByteArray? = null,
    progressRle: String? = null,
): WorkLocalItem {
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

private fun WorkDetailsPreview.toEntity(
    image: ByteArray? = null,
    progressRle: String? = null,
): WorkEntity {
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
        gridRle = progressRle,
        percentage = percentage,
        spendedTime = spendedTime,
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

