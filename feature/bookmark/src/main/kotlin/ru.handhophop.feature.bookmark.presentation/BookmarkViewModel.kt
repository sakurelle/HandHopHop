package ru.handhophop.feature.bookmark.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import ru.handhophop.core.system.database.work.WorkLocalItem
import ru.handhophop.core.system.database.work.WorkLocalRepository

@Suppress("UNCHECKED_CAST")
internal class BookmarkViewModel(
    private val repository: WorkLocalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookmarkUiState>(BookmarkUiState.Loading)
    val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeWorkDataVersion()
                .drop(1)
                .collect {
                    refreshBookmarks()
                }
        }
    }

    fun loadBookmarks() {
        refreshBookmarks(forceLoading = true)
    }

    fun onFilterSelected(filter: BookmarkFilter) {
        applyFilter(filter)
    }

    fun onFavoriteClick(photo: BookmarkPhotoItem) {
        if (!photo.canBookmark) {
            return
        }

        viewModelScope.launch {
            val newIsBookmarked = !photo.isBookmarked

            runCatching {
                if (newIsBookmarked) {
                    repository.addFavorite(
                        WorkLocalItem(
                            id = photo.id,
                            url = photo.photoUrl,
                            imagePath = photo.imagePath,
                            isFavorite = true,
                        ),
                    )
                } else {
                    repository.removeFavorite(
                        photo.bookmarkTargetKey(),
                    )
                }
            }.onSuccess {
                updateFavoriteState(
                    photo = photo,
                    isBookmarked = newIsBookmarked,
                )
            }
        }
    }

    private fun refreshBookmarks(
        forceLoading: Boolean = false,
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value as? BookmarkUiState.Success
            val selectedFilter = currentState?.selectedFilter ?: BookmarkFilter.ALL

            if (forceLoading || currentState == null) {
                _uiState.value = BookmarkUiState.Loading
            }

            runCatching {
                repository.getBookmarkPreviews()
                    .asSequence()
                    .map { work ->
                        val photoUrl = work.url.orEmpty()
                        val hasOnlineUrl = photoUrl.isOnlinePhotoUrl()
                        val hasLocalImage = !work.imagePath.isNullOrBlank()

                        BookmarkPhotoItem(
                            id = work.id,
                            photoUrl = photoUrl,
                            imagePath = work.imagePath,
                            isBookmarked = work.isFavorite,
                            canBookmark = hasOnlineUrl || hasLocalImage,
                            isStarted = work.isStarted,
                            progressPercentage = work.percentage ?: 0,
                            projectName = work.projectName,
                        )
                    }
                    .toList()
                    .mergeDuplicatePhotos()
            }.fold(
                onSuccess = { photos ->
                    val allPhotos = photos.toImmutableList()
                    _uiState.value = BookmarkUiState.Success(
                        allPhotos = allPhotos,
                        photos = applyFilterToPhotos(allPhotos, selectedFilter),
                        selectedFilter = selectedFilter,
                    )
                },
                onFailure = { error ->
                    _uiState.value = BookmarkUiState.Error(
                        message = error.message ?: "Unknown error",
                    )
                },
            )
        }
    }

    private fun applyFilter(filter: BookmarkFilter) {
        val currentState = _uiState.value as? BookmarkUiState.Success ?: return
        _uiState.value = currentState.copy(
            photos = applyFilterToPhotos(currentState.allPhotos, filter),
            selectedFilter = filter,
        )
    }

    private fun updateFavoriteState(
        photo: BookmarkPhotoItem,
        isBookmarked: Boolean,
    ) {
        val currentState = _uiState.value as? BookmarkUiState.Success ?: return
        val targetKey = photo.stablePhotoKey()

        val allPhotos = currentState.allPhotos.map { currentPhoto ->
            if (currentPhoto.stablePhotoKey() == targetKey) {
                currentPhoto.copy(isBookmarked = isBookmarked)
            } else {
                currentPhoto
            }
        }.filter { currentPhoto ->
            currentPhoto.isBookmarked || currentPhoto.isStarted
        }.toImmutableList()

        _uiState.value = currentState.copy(
            allPhotos = allPhotos,
            photos = applyFilterToPhotos(allPhotos, currentState.selectedFilter),
        )
    }

    class Factory(
        private val repository: WorkLocalRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BookmarkViewModel(repository) as T
        }
    }
}

private fun applyFilterToPhotos(
    photos: ImmutableList<BookmarkPhotoItem>,
    filter: BookmarkFilter,
): ImmutableList<BookmarkPhotoItem> {
    return when (filter) {
        BookmarkFilter.ALL -> photos
        BookmarkFilter.LIKES -> photos.filter { it.isBookmarked }.toImmutableList()
        BookmarkFilter.WORKS -> photos.filter { it.isStarted }.toImmutableList()
    }
}

private fun String.isOnlinePhotoUrl(): Boolean {
    return startsWith("http://") || startsWith("https://")
}

private fun List<BookmarkPhotoItem>.mergeDuplicatePhotos(): List<BookmarkPhotoItem> {
    return groupBy(BookmarkPhotoItem::stablePhotoKey)
        .values
        .map { duplicates ->
            val preferred = duplicates.maxWithOrNull(
                compareBy<BookmarkPhotoItem>(
                    { if (it.isStarted) 1 else 0 },
                    { if (it.progressPercentage > 0) 1 else 0 },
                    { if (it.isBookmarked) 1 else 0 },
                    { if (it.projectName.isNullOrBlank()) 0 else 1 },
                    { it.id },
                ),
            ) ?: duplicates.first()

            preferred.copy(
                photoUrl = preferred.photoUrl
                    .takeIf { it.isNotBlank() }
                    ?: duplicates.firstNotNullOfOrNull { photo ->
                        photo.photoUrl.takeIf { it.isNotBlank() }
                    }
                    .orEmpty(),
                imagePath = preferred.imagePath
                    ?.takeIf { it.isNotBlank() }
                    ?: duplicates.firstNotNullOfOrNull { photo ->
                        photo.imagePath?.takeIf { it.isNotBlank() }
                    },
                isBookmarked = duplicates.any(BookmarkPhotoItem::isBookmarked),
                canBookmark = duplicates.any(BookmarkPhotoItem::canBookmark),
                isStarted = duplicates.any(BookmarkPhotoItem::isStarted),
                progressPercentage = duplicates.maxOf(BookmarkPhotoItem::progressPercentage),
                projectName = preferred.projectName
                    ?.takeIf { it.isNotBlank() }
                    ?: duplicates.firstNotNullOfOrNull { photo ->
                        photo.projectName?.takeIf { it.isNotBlank() }
                    },
            )
        }
        .sortedByDescending(BookmarkPhotoItem::id)
}

private fun BookmarkPhotoItem.stablePhotoKey(): String {
    return imagePath
        ?.takeIf { it.isNotBlank() }
        ?: photoUrl.normalizedPhotoUrlKey()
            .ifBlank { "local:$id" }
}

private fun BookmarkPhotoItem.bookmarkTargetKey(): String {
    return imagePath?.takeIf { it.isNotBlank() } ?: photoUrl
}

private fun String.normalizedPhotoUrlKey(): String {
    return trim().replace("http://", "https://")
}
