package ru.handhophop.feature.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.handhophop.feature.feed.data.FeedRepository
import java.util.concurrent.atomic.AtomicInteger

internal class FeedViewModel(
    private val repository: FeedRepository,
): ViewModel() {
    private val curPage = AtomicInteger(1)
    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState
    fun handleAction(action: FeedUiAction) {
        when (action) {
            is FeedUiAction.LoadPhotos -> loadPhotosIfNeeded()
            is FeedUiAction.Refresh -> refreshPhotos()
            is FeedUiAction.LoadNextPage -> loadNextPage()
        }
    }

    private fun refreshPhotos() {
        repository.clearCache()
        loadPhotos(refresh = true)
    }

    private fun loadPhotosIfNeeded() {
        val current = _uiState.value
        if (current is FeedUiState.Success && current.photos.isNotEmpty()) return

        loadPhotos(refresh = false)
    }

    private fun loadPhotos(refresh: Boolean) {
        if (refresh) {
            repository.clearCache()
            curPage.set(1)
        }

        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading

            repository.getPhotos().fold(
                onSuccess = { photos ->
                    val items = photos.map { FeedPhotoItem(id = it.id.toString(), photoUrl = it.image.source.url.replace("http://", "https://")) }
                    _uiState.value = FeedUiState.Success(photos=items, isRecommendedLoading = true, hasNext = items.isNotEmpty())
                },
                onFailure = { error ->
                    _uiState.value = FeedUiState.Error(reason = mapError(error), msg = error.message ?: "Unknown error")
                }
            )

            val current = _uiState.value as? FeedUiState.Success ?: return@launch
            repository.getRecommendedPhotos().fold(

                onSuccess = { photos ->
                    val items = photos.map { FeedPhotoItem(id = it.id.toString(), photoUrl = it.image.source.url.replace("http://", "https://"))}
                    _uiState.value = current.copy(
                        recommendedPhotos = items,
                        isRecommendedLoading = false
                    )

                },
                onFailure = {
                    _uiState.value = current.copy(isRecommendedLoading = false)
                }
            )

        }
    }
    private fun loadNextPage() {
        var nextPage = -1

        _uiState.update { current ->
            if (current !is FeedUiState.Success) return@update current
            if (!current.hasNext || current.isLoadingMore) return@update current
            nextPage = curPage.updateAndGet { page ->
                if (page >= 100) 1 else page+1
            }
            current.copy(isLoadingMore = true)
        }

        if (nextPage == -1) return

        viewModelScope.launch {
            repository.getPhotos(page = nextPage, count = 10).fold(
                onSuccess = { photos ->
                    val newPhotos = photos.map { FeedPhotoItem(id = it.id.toString(), photoUrl = it.image.source.url.replace("http://", "https://")) }

                    _uiState.update { current ->
                        if (current !is FeedUiState.Success) return@update current
                        current.copy(
                            photos = current.photos + newPhotos,
                            hasNext = newPhotos.isNotEmpty(),
                            isLoadingMore = false
                        )
                    }


                },
                onFailure = {
                    _uiState.update { current ->
                        if (current !is FeedUiState.Success) return@update current
                        current.copy(isLoadingMore = false)
                    }
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
