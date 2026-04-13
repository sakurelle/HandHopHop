package ru.handhophop.feature.feed.presentation

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface FeedUiState {
    @Immutable
    data object Loading: FeedUiState

    @Immutable
    data class Success(
        val photos: List<FeedPhotoItem> = emptyList(),
        val hasNext: Boolean = true,
        val isLoadingMore: Boolean = false,

        val recommendedPhotos: List<FeedPhotoItem> = emptyList(),
        val isRecommendedLoading: Boolean = false
    ) : FeedUiState

    @Immutable
    data class Error(
        val reason: FeedError,
        val msg: String
    ): FeedUiState
}

@Immutable
internal sealed interface FeedError {
    data object NetworkUnavailable: FeedError
    data object LoadingFailure: FeedError
    data object Default: FeedError
}

@Immutable
internal data class FeedPhotoItem(
    val id: String,
    val photoUrl: String
)