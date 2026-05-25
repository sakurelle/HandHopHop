package ru.handhophop.core.system.database.work

data class WorkBookmarkPreview(
    val id: Long,
    val url: String?,
    val imagePath: String?,
    val isFavorite: Boolean,
    val isStarted: Boolean,
    val percentage: Int?,
    val projectName: String?,
)
