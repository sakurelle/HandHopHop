package ru.handhophop.core.system.database.work

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
    val spendedTime: Long? = null,
)

class WorkLocalRepository(
    private val workDao: WorkDao,
) {

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
        val current = workDao.getByUrl(url) ?: return
        if (current.hasStartedWork()) {
            workDao.update(current.copy(isFavorite = false))
        } else {
            workDao.delete(current)
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
                spendedTime = work.spendedTime,
            )
        }
    }

    suspend fun removeWork(id: Long) {
        val current = workDao.getById(id) ?: return
        if (current.isFavorite) {
            workDao.update(
                current.copy(
                    projectName = null,
                    schemeType = null,
                    colorCount = null,
                    difficulty = null,
                    gridWidth = null,
                    gridHeight = null,
                    gridRle = null,
                    percentage = null,
                    spendedTime = null,
                )
            )
        } else {
            workDao.delete(current)
        }
    }

    suspend fun getAllWorks(): List<WorkLocalItem> {
        return workDao.getAll().map(WorkEntity::toLocalItem)
    }

    suspend fun getWorkByUrl(url: String): WorkLocalItem? {
        return workDao.getByUrl(url)?.toLocalItem()
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
            spendedTime != null
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
        spendedTime = spendedTime,
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
        spendedTime = spendedTime,
    )
}

private fun WorkEntity.hasStartedWork(): Boolean {
    return projectName != null ||
            schemeType != null ||
            colorCount != null ||
            difficulty != null ||
            gridWidth != null ||
            gridHeight != null ||
            gridRle != null ||
            percentage != null ||
            spendedTime != null
}
