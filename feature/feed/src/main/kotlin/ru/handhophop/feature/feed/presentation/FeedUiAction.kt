package ru.handhophop.feature.feed.presentation

internal sealed interface FeedUiAction {
    data object LoadPhotos: FeedUiAction
    data object Refresh: FeedUiAction
    data object LoadNextPage: FeedUiAction
    data class PhotoClicked(val id: String): FeedUiAction
    data class ApplyFilter (val sectionId: Int, val optionId: Int): FeedUiAction
    data class SetFilterVisibility(val isVisible: Boolean): FeedUiAction
}