package ru.handhophop.feature.bookmark.presentation

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface BookmarkUiState {
    @Immutable
    data object Loading : BookmarkUiState

    @Immutable
    data class Success(
        val photos: List<BookmarkPhotoItem> = emptyList(),
    ) : BookmarkUiState

    @Immutable
    data class Error(
        val message: String,
    ) : BookmarkUiState
}

@Immutable
internal data class BookmarkPhotoItem(
    val id: Long,
    val photoUrl: String,
    val imageBytes: ByteArray? = null,
)
