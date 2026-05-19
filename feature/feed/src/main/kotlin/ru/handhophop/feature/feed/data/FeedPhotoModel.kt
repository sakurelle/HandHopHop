package ru.handhophop.feature.feed.data

internal class FeedPhotoModel (
    val id: String,
    val photoUrl: String,
    val isBookmarked: Boolean,
    val isStarted: Boolean,
    val progressPercentage: Int,
)