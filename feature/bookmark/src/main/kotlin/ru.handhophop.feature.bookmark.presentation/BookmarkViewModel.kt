package ru.handhophop.feature.bookmark.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.handhophop.core.system.database.work.WorkLocalItem
import ru.handhophop.core.system.database.work.WorkLocalRepository

@Suppress("UNCHECKED_CAST")
internal class BookmarkViewModel(
    private val repository: WorkLocalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookmarkUiState>(BookmarkUiState.Loading)
    val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()

    fun loadBookmarks() {
        viewModelScope.launch {
            _uiState.value = BookmarkUiState.Loading

            runCatching {
                repository.getBookmarkPreviews()
                    .asSequence()
                    .map { work ->
                        val photoUrl = work.url.orEmpty()
                        val canBookmark = photoUrl.isOnlinePhotoUrl()

                        BookmarkPhotoItem(
                            id = work.id,
                            photoUrl = photoUrl,
                            imagePath = work.imagePath,
                            isBookmarked = canBookmark && work.isFavorite,
                            canBookmark = canBookmark,
                            isStarted = work.isStarted,
                            progressPercentage = work.percentage ?: 0,
                            projectName = work.projectName,
                        )
                    }
                    .toList()
            }.fold(
                onSuccess = { photos ->
                    val allPhotos = photos.toImmutableList()

                    _uiState.value = BookmarkUiState.Success(
                        allPhotos = allPhotos,
                        photos = allPhotos,
                        selectedFilter = BookmarkFilter.ALL,
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
                            isFavorite = true,
                        ),
                    )
                } else {
                    repository.removeFavorite(photo.photoUrl)
                }
            }.onSuccess {
                updateFavoriteState(
                    photoUrl = photo.photoUrl,
                    isBookmarked = newIsBookmarked,
                )
            }
        }
    }

    private fun applyFilter(filter: BookmarkFilter) {
        val currentState = _uiState.value as? BookmarkUiState.Success ?: return
        val allPhotos = currentState.allPhotos

        val filteredPhotos = when (filter) {
            BookmarkFilter.ALL -> allPhotos
            BookmarkFilter.LIKES -> allPhotos.filter { it.isBookmarked }.toImmutableList()
            BookmarkFilter.WORKS -> allPhotos.filter { it.isStarted }.toImmutableList()
        }

        _uiState.value = currentState.copy(
            photos = filteredPhotos,
            selectedFilter = filter,
        )
    }

    private fun updateFavoriteState(
        photoUrl: String,
        isBookmarked: Boolean,
    ) {
        val currentState = _uiState.value as? BookmarkUiState.Success ?: return

        val allPhotos = currentState.allPhotos.map { photo ->
            if (photo.photoUrl == photoUrl) {
                photo.copy(isBookmarked = isBookmarked)
            } else {
                photo
            }
        }.filter { photo ->
            photo.isBookmarked || photo.isStarted
        }.toImmutableList()

        val filteredPhotos = when (currentState.selectedFilter) {
            BookmarkFilter.ALL -> allPhotos
            BookmarkFilter.LIKES -> allPhotos.filter { it.isBookmarked }.toImmutableList()
            BookmarkFilter.WORKS -> allPhotos.filter { it.isStarted }.toImmutableList()
        }

        _uiState.value = currentState.copy(
            allPhotos = allPhotos,
            photos = filteredPhotos,
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

private fun String.isOnlinePhotoUrl(): Boolean {
    return startsWith("http://") || startsWith("https://")
}
