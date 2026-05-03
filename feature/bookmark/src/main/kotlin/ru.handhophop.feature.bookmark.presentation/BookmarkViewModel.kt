package ru.handhophop.feature.bookmark.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.handhophop.core.system.database.work.WorkLocalRepository

internal class BookmarkViewModel(
    private val repository: WorkLocalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookmarkUiState>(BookmarkUiState.Loading)
    val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()

    fun loadBookmarks() {
        viewModelScope.launch {
            _uiState.value = BookmarkUiState.Loading

            runCatching {
                repository.getFavoriteWorks()
                    .asSequence()
                    .map { work ->
                        BookmarkPhotoItem(
                            id = work.id,
                            photoUrl = work.url,
                            imageBytes = work.image,
                        )
                    }
                    .toList()
            }.fold(
                onSuccess = { photos ->
                    _uiState.value = BookmarkUiState.Success(
                        photos = photos,
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

    class Factory(
        private val repository: WorkLocalRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BookmarkViewModel(repository) as T
        }
    }
}
