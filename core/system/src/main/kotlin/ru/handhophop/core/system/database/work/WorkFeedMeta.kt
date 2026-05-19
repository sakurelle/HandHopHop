package ru.handhophop.core.system.database.work

data class WorkFeedMeta(
    val url: String?,
    val isFavorite: Boolean,
    val isStarted: Boolean,
    val percentage: Int?,
    val projectName: String?,
)