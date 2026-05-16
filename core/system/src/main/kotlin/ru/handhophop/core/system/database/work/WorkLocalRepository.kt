package ru.handhophop.core.system.database.work

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import java.io.File

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
        workDao.deleteAll()
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

    suspend fun addWork(work: WorkLocalItem): Long {
        return upsert(work) { current ->
            current.copy(
                url = work.url,
                image = work.image ?: current.image,
                projectName = work.projectName,
                schemeType = work.schemeType,
                colorCount = work.colorCount,
                difficulty = work.difficulty,
                gridWidth = work.gridWidth,
                gridHeight = work.gridHeight,
                gridRle = work.gridRle,
                percentage = work.percentage,
                spendedTime = work.spentTime,
            )
        }
    }

    suspend fun removeWork(id: Long) {
        workDao.deleteById(id)
    }

    suspend fun getAllWorks(): List<WorkLocalItem> {
        return workDao.getAll().map(WorkEntity::toLocalItem)
    }

    suspend fun getFavoriteWorks(): List<WorkLocalItem> {
        return workDao.getFavorites().map(WorkEntity::toLocalItem)
    }

    suspend fun getWorkByUrl(url: String): WorkLocalItem? {
        return workDao.getByUrl(url)?.toLocalItem()
    }

    suspend fun getWorksByUrls(urls: List<String>): List<WorkLocalItem> {
        if (urls.isEmpty()) return emptyList()
        return workDao.getByUrls(urls).map(WorkEntity::toLocalItem)
    }

    suspend fun getWorkById(id: Long): WorkLocalItem? {
        return workDao.getById(id)?.toLocalItem()
    }

    private suspend fun upsert(
        work: WorkLocalItem,
        updateCurrent: (WorkEntity) -> WorkEntity,
    ): Long {
        val current = when {
            work.id > 0 -> workDao.getById(work.id)
            else -> workDao.getByUrl(work.url)
        }

        return if (current == null) {
            workDao.insert(work.toEntity())
        } else {
            workDao.update(updateCurrent(current))
            current.id
        }
    }
}

fun WorkLocalItem.hasStartedWork(): Boolean {
    return projectName != null ||
            schemeType != null ||
            colorCount != null ||
            difficulty != null ||
            gridWidth != null ||
            gridHeight != null ||
            gridRle != null ||
            percentage != null ||
            spentTime != null
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

private fun WorkEntity.toLocalItem(): WorkLocalItem {
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
        gridRle = gridRle,
        percentage = percentage,
        spentTime = spendedTime,
    )
}

