package ru.handhophop.feature.bookmark.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal sealed interface BookmarkUiState {
    @Immutable
    data object Loading : BookmarkUiState

    @Immutable
    data class Success(
        val allPhotos: ImmutableList<BookmarkPhotoItem> = persistentListOf(),
        val photos: ImmutableList<BookmarkPhotoItem> = persistentListOf(),
        val selectedFilter: BookmarkFilter = BookmarkFilter.ALL,
    ) : BookmarkUiState

    @Immutable
    data class Error(
        val message: String,
    ) : BookmarkUiState
}

@Immutable
internal enum class BookmarkFilter {
    ALL,
    WORKS,
    LIKES,
}

@Immutable
internal data class BookmarkPhotoItem(
    val id: Long,
    val photoUrl: String,
    val imagePath: String? = null,
    val isBookmarked: Boolean = false,
    val isStarted: Boolean = false,
    val progressPercentage: Int = 0,
    val projectName: String? = null,
)
