package ru.handhophop.feature.mash.completed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.handhophop.core.network.api.WallhavenApiService
import ru.handhophop.core.system.database.work.WorkLocalItem
import ru.handhophop.core.system.database.work.WorkLocalRepository

private const val COMPLETED_RECOMMENDATIONS_LIMIT = 6
private const val COMPLETED_RECOMMENDATIONS_PAGE = 1
private const val WALLHAVEN_CATEGORY_GENERAL = "100"
private const val WALLHAVEN_PURITY_SAFE = "100"
private const val WALLHAVEN_SORTING_RANDOM = "random"

internal class WorkCompletedViewModel(
    private val apiService: WallhavenApiService,
    private val workLocalRepository: WorkLocalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkCompletedUiState())
    val uiState: StateFlow<WorkCompletedUiState> = _uiState.asStateFlow()

    fun loadRecommendations() {
        if (_uiState.value.recommendations.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            runCatching {
                val photos = apiService.searchWallpapers(
                    query = null,
                    categories = WALLHAVEN_CATEGORY_GENERAL,
                    purity = WALLHAVEN_PURITY_SAFE,
                    sorting = WALLHAVEN_SORTING_RANDOM,
                    page = COMPLETED_RECOMMENDATIONS_PAGE,
                ).data.take(COMPLETED_RECOMMENDATIONS_LIMIT)

                val normalizedPhotos = photos.map { photo ->
                    photo to photo.path.normalizePhotoUrl()
                }

                val urls = normalizedPhotos.map { it.second }

                val metaByUrl = workLocalRepository
                    .getFeedMetaByUrls(urls)
                    .associateBy { it.url.orEmpty().normalizePhotoUrl() }

                normalizedPhotos.map { (photo, normalizedUrl) ->
                    WorkCompletedRecommendationItem(
                        id = photo.id,
                        photoUrl = normalizedUrl,
                        isBookmarked = metaByUrl[normalizedUrl]?.isFavorite == true,
                    )
                }
            }.fold(
                onSuccess = { recommendations ->
                    _uiState.value = WorkCompletedUiState(
                        isLoading = false,
                        recommendations = recommendations,
                    )
                },
                onFailure = {
                    _uiState.update { state ->
                        state.copy(isLoading = false)
                    }
                },
            )
        }
    }

    fun onFavoriteClick(item: WorkCompletedRecommendationItem) {
        val newIsBookmarked = !item.isBookmarked

        updateFavoriteState(
            photoUrl = item.photoUrl,
            isBookmarked = newIsBookmarked,
        )

        viewModelScope.launch {
            runCatching {
                if (newIsBookmarked) {
                    workLocalRepository.addFavorite(
                        WorkLocalItem(
                            url = item.photoUrl,
                            isFavorite = true,
                        ),
                    )
                } else {
                    workLocalRepository.removeFavorite(item.photoUrl)
                }
            }.onFailure {
                updateFavoriteState(
                    photoUrl = item.photoUrl,
                    isBookmarked = item.isBookmarked,
                )
            }
        }
    }

    private fun updateFavoriteState(
        photoUrl: String,
        isBookmarked: Boolean,
    ) {
        _uiState.update { state ->
            state.copy(
                recommendations = state.recommendations.map { item ->
                    if (item.photoUrl == photoUrl) {
                        item.copy(isBookmarked = isBookmarked)
                    } else {
                        item
                    }
                },
            )
        }
    }

    private fun String.normalizePhotoUrl(): String {
        return replace("http://", "https://")
    }

    class Factory(
        private val apiService: WallhavenApiService,
        private val workLocalRepository: WorkLocalRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WorkCompletedViewModel(
                apiService = apiService,
                workLocalRepository = workLocalRepository,
            ) as T
        }
    }
}