package ru.handhophop.feature.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class FeedViewModel(
    private val repository: FeedRepository
): ViewModel() {
    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState

   fun handleAction(action: FeedUiAction) {
        when (action) {
            is FeedUiAction.LoadPhotos -> loadPhotos()
            is FeedUiAction.Refresh -> loadPhotos()
            is FeedUiAction.PhotoClicked -> { /*какое-то действие, пока не трогаю*/ }
        }
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading

            repository.getPhotos().fold(
                onSuccess = { photos ->
                    val items = photos.map { FeedPhotoItem(id = it.id, photoUrl = it.urls.regular) }
                    _uiState.value = FeedUiState.Success(photos=items)
                },
                onFailure = { error ->
                    val feedError = when {
                        error is java.net.UnknownHostException -> FeedError.NetworkUnavailable
                        error is java.io.IOException -> FeedError.LoadingFailure
                        else -> FeedError.Default
                    }
                    _uiState.value = FeedUiState.Error(reason = feedError, msg = error.message ?: "Unknown error")
                }
            )
        }
    }

    class Factory(private val repository: FeedRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FeedViewModel(repository) as T
        }
    }
}