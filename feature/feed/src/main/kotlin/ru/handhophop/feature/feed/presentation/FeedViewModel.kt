package ru.handhophop.feature.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.handhophop.feature.feed.data.FeedRepository

internal class FeedViewModel(
    private val repository: FeedRepository
): ViewModel() {
    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState

   fun handleAction(action: FeedUiAction) {
        when (action) {
            is FeedUiAction.LoadPhotos -> loadPhotos()
            is FeedUiAction.Refresh -> loadPhotos()
            is FeedUiAction.LoadNextPage -> loadNextPage()
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
                    _uiState.value = FeedUiState.Error(reason = mapError(error), msg = error.message ?: "Unknown error")
                }
            )
        }
    }

    private fun loadNextPage() {
        val currentUiState = _uiState.value as? FeedUiState.Success ?: return //если состояние неуспех, то ниче не возвращаем
        if (!currentUiState.hasNext || currentUiState.isLoadingMore) return

        viewModelScope.launch {
            _uiState.value = currentUiState.copy(isLoadingMore = true) //копия состояния с нужными параметрами

            repository.getPhotos(count = 10).fold(
                onSuccess = { photos ->
                    val newPhotos = photos.map { FeedPhotoItem(id = it.id, photoUrl = it.urls.regular) }
                    _uiState.value = currentUiState.copy(
                        photos = currentUiState.photos+newPhotos,
                        hasNext = photos.size >= 10,
                        isLoadingMore = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = currentUiState.copy(isLoadingMore = false)

                }
            )

        }
    }

    private fun mapError(error: Throwable): FeedError = when {
        error is java.net.UnknownHostException -> FeedError.NetworkUnavailable
        error is java.io.IOException -> FeedError.LoadingFailure
        else -> FeedError.Default
    }

    class Factory(private val repository: FeedRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FeedViewModel(repository) as T
        }
    }
}