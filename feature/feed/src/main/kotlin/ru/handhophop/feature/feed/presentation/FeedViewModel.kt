package ru.handhophop.feature.feed.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.handhophop.feature.feed.R
import ru.handhophop.feature.feed.data.FeedRepository
import java.util.concurrent.atomic.AtomicInteger

internal class FeedViewModel(
    private val repository: FeedRepository,
): ViewModel() {
    private companion object {
        const val TAG = "FeedViewModel"
        const val SECTION_ORIENTATION = 0
        const val SECTION_COLOR = 1
        const val SECTION_AI = 2
    }
    private val curPage = AtomicInteger(1)
    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState
    private val currentFilter: FeedFilter get() = (_uiState.value as? FeedUiState.Success)?.currentFilter?: FeedFilter()

    fun handleAction(action: FeedUiAction) {
        when (action) {
            is FeedUiAction.LoadPhotos -> loadPhotosIfNeeded()
            is FeedUiAction.Refresh -> refreshPhotos()
            is FeedUiAction.LoadNextPage -> loadNextPage()
            is FeedUiAction.PhotoClicked -> { /*какое-то действие, пока не трогаю*/ }
            is FeedUiAction.ApplyFilter -> aplyFilter(action.sectionId, action.optionId)
            is FeedUiAction.SetFilterVisibility -> setFilterVisibility(action.isVisible)
        }
    }

    private fun setFilterVisibility(isVisible: Boolean) {
        _uiState.update { current ->
            if (current !is FeedUiState.Success) return@update current
            current.copy(isFilterVisible = isVisible)
        }
    }

    private fun aplyFilter(sectionId: Int, optionId: Int) {
        val newFilter = when (sectionId) {
            SECTION_ORIENTATION -> {
                val orientation = OrientationFilter.entries.first {it.id == optionId}
                currentFilter.copy(orientation=orientation)
            }
            SECTION_COLOR -> {
                val color = ColorFilter.entries.first {it.id == optionId}
                currentFilter.copy(color = color)
            }
            SECTION_AI -> {
                val ai = AiGeneratedFilter.entries.first {it.id == optionId}
                currentFilter.copy(aiGenerated = ai)
            }
            else -> currentFilter
        }

        _uiState.update { current ->
            if (current !is FeedUiState.Success) return@update current
            current.copy(currentFilter = newFilter)
        }

        repository.clearCache()
        curPage.set(1)
        loadPhotos(refresh = true)
    }

    private fun setFilterSections(filter: FeedFilter): List<FilterSectionState> {
        return listOf(
            FilterSectionState(
                sectionId = SECTION_ORIENTATION,
                titleRes = R.string.orientation_title,
                options = OrientationFilter.entries.map { entry ->
                    FilterOptionState(
                        textRes = entry.labelRes,
                        id = entry.id,
                        isSelected = filter.orientation == entry
                    )
                }
            ),

            FilterSectionState(
                sectionId = SECTION_COLOR,
                titleRes = R.string.color_title,
                options = ColorFilter.entries.map { entry ->
                    FilterOptionState(
                        textRes = entry.labelRes,
                        id = entry.id,
                        isSelected = filter.color == entry
                    )
                }
            ),

            FilterSectionState(
                sectionId = SECTION_AI,
                titleRes = R.string.ai_title,
                options = AiGeneratedFilter.entries.map { entry ->
                    FilterOptionState(
                        textRes = entry.labelRes,
                        id = entry.id,
                        isSelected = filter.aiGenerated == entry
                    )
                }
            )
        )
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
        val filterCopy = currentFilter
        if (refresh) {
            repository.clearCache()
            curPage.set(1)
        }

        viewModelScope.launch {
            Log.d(TAG, "Loading photos. refresh=$refresh")
            _uiState.value = FeedUiState.Loading

            repository.getPhotos(
                orientationId = filterCopy.orientation.id,
                colorId = filterCopy.color.id,
                aiId = filterCopy.aiGenerated.id
                ).fold(
                onSuccess = { photos ->
                    Log.d(TAG, "Main feed loaded successfully. count=${photos.size}")
                    val items = photos.map {
                        FeedPhotoItem(
                            id = it.id.toString(),
                            photoUrl = it.image.source.url.replace("http://", "https://")
                        )
                    }
                    _uiState.value = FeedUiState.Success(
                        photos = items,
                        isRecommendedLoading = true,
                        hasNext = items.isNotEmpty(),
                        filterSections = setFilterSections(filterCopy),
                        currentFilter = filterCopy
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Main feed load failed", error)
                    _uiState.value = FeedUiState.Error(reason = mapError(error), msg = error.message ?: "Unknown error")
                }
            )

            val current = _uiState.value as? FeedUiState.Success ?: return@launch
            repository.getRecommendedPhotos().fold(

                onSuccess = { photos ->
                    Log.d(TAG, "Recommended photos loaded successfully. count=${photos.size}")
                    val items = photos.map {
                        FeedPhotoItem(
                            id = it.id.toString(),
                            photoUrl = it.image.source.url.replace("http://", "https://")
                        )
                    }
                    _uiState.update { current ->
                        if (current !is FeedUiState.Success) return@update current
                        current.copy(
                            recommendedPhotos = items,
                            isRecommendedLoading = false
                        )
                    }

                },
                onFailure = { error ->
                    Log.e(TAG, "Recommended photos load failed", error)
                    _uiState.update { current ->
                        if (current !is FeedUiState.Success) return@update current
                        current.copy(isRecommendedLoading = false)
                    }
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
                if (page >= 100) 1 else page + 1
            }
            current.copy(isLoadingMore = true)
        }

        if (nextPage == -1) return

        viewModelScope.launch {
            Log.d(TAG, "Loading next page: page=$nextPage")
            repository.getPhotos(
                page = nextPage,
                count = 10,
                orientationId = currentFilter.orientation.id,
                colorId = currentFilter.color.id,
                aiId = currentFilter.aiGenerated.id
                ).fold(
                onSuccess = { photos ->
                    Log.d(TAG, "Next page loaded successfully. page=$nextPage, count=${photos.size}")
                    val newPhotos = photos.map {
                        FeedPhotoItem(
                            id = it.id.toString(),
                            photoUrl = it.image.source.url.replace("http://", "https://")
                        )
                    }

                    _uiState.update { current ->
                        if (current !is FeedUiState.Success) return@update current
                        current.copy(
                            photos = current.photos + newPhotos,
                            hasNext = newPhotos.isNotEmpty(),
                            isLoadingMore = false
                        )
                    }


                },
                onFailure = { error ->
                    Log.e(TAG, "Next page load failed. page=$nextPage", error)
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