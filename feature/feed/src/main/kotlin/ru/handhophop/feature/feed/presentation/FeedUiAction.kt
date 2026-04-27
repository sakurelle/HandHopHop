package ru.handhophop.feature.feed.presentation

internal sealed interface FeedUiAction {
    data object LoadPhotos: FeedUiAction
    data object Refresh: FeedUiAction
    data object LoadNextPage: FeedUiAction
}
