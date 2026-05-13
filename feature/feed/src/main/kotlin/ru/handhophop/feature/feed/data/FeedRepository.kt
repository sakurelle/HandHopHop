package ru.handhophop.feature.feed.data

import android.util.Log
import ru.handhophop.core.network.api.WallhavenApiService
import ru.handhophop.core.network.models.WallhavenPhoto
import ru.handhophop.core.system.database.work.WorkLocalItem
import ru.handhophop.core.system.database.work.WorkLocalRepository

internal class FeedRepository(
    private val apiService: WallhavenApiService,
    private val workLocalRepository: WorkLocalRepository,
) {
    private companion object {
        const val TAG = "FeedRepository"
    }

    private val feedTerms = listOf(
        "nature",
        "sea",
        "mountains",
        "forest",
        "cats",
        "dogs",
        "sunset",
        "ocean",
        "flowers",
        "waterfall",
        "lake",
        "beach",
        "sky",
        "wildlife",
        "island",
        "snow",
        "desert",
        "river",
        "garden",
        "birds"
    )

    private var currentTerm: String = feedTerms.random()
    private val cachedIds = mutableSetOf<String>()

    private fun String.normalizePhotoUrl(): String {
        return replace("http://", "https://")
    }

    private suspend fun fillPhotosWithLocalData(
        photos: List<WallhavenPhoto>
    ): List<FeedPhotoModel> {
        val normalizedPhotos = photos.map { photo ->
            photo to photo.path.normalizePhotoUrl()
        }

        val urls = normalizedPhotos.map { it.second }

        val worksByUrl = workLocalRepository
            .getFeedMetaByUrls(urls)
            .associateBy{ it.url.orEmpty().normalizePhotoUrl() }

        return normalizedPhotos.map { (photo, normalizedUrl) ->
            val localWork = worksByUrl[normalizedUrl]

            val hasStarted = localWork?.isStarted == true

            FeedPhotoModel(
                id = photo.id,
                photoUrl = normalizedUrl,
                isBookmarked = localWork?.isFavorite == true,
                isStarted = hasStarted,
                progressPercentage = if (hasStarted) { localWork.percentage ?: 0} else {0}
            )

        }

    }

    suspend fun getPhotos(
        page: Int = 1,
        count: Int = 10,
        orientationId: Int = 0,
        colorId: Int = 0,
        categoryCode: String = "100",
        sorting: String = "random"
    ): Result<List<FeedPhotoModel>> {
        return runCatching {
            val colorParam = when (colorId) {
                0  -> null
                1  -> "000000"  // black
                2  -> "0000ff"  // blue
                3  -> "808080"  // gray
                4  -> "008000"  // green
                5  -> "ffa500"  // orange
                6  -> "ff0000"  // red
                7  -> "ffffff"  // white
                8  -> "ffff00"  // yellow
                9  -> "800080"  // purple
                10 -> "00ffff"  // cyan
                11 -> "ffc0cb"  // pink
                else -> null
            }

            // Wallhaven uses ratios instead of orientation flags
            // Supported ratios: 16x9, 16x10, 4x3, 3x2, 2x1, 21x9, 32x9, 48x9, 9x16, 10x16, etc.
            val ratios = when (orientationId) {
                1 -> "landscape"      // landscape - 16x9, 16x10, 4x3, etc.
                2 -> "portrait"       // portrait - 9x16, 10x16, etc.
                3 -> "1x1"            // square
                4 -> "21x9,32x9,48x9" // panoramic - ultra-wide ratios
                else -> null
            }

            val photos = apiService.searchWallpapers(
                query = currentTerm,
                categories = categoryCode,
                purity = "100",      // SFW only
                sorting = sorting,
                ratios = ratios,
                colors = colorParam,
                page = page
            ).data

            val unique = photos.filter { it.id !in cachedIds }
            cachedIds.addAll(unique.map { it.id })
            Log.d(TAG, "Received photos: total=${photos.size}, unique=${unique.size}, filter=$currentTerm")
            fillPhotosWithLocalData(unique)
        }.onFailure { error ->
            Log.e(TAG, "Failed to load photos for page=$page", error)
        }
    }

    suspend fun getRecommendedPhotos(): Result<List<FeedPhotoModel>> {
        return runCatching {
            Log.d(TAG, "Requesting recommended photos")
            val photos = apiService.searchWallpapers(
                query = null,
                categories = "100",
                purity = "100",
                sorting = "random",
                page = 1
            ).data.take(5)
            Log.d(TAG, "Received recommended photos, count=${photos.size}")
            fillPhotosWithLocalData(photos)
        }.onFailure { error ->
            Log.e(TAG, "Failed to load recommended photos", error)
        }
    }

    suspend fun setFavorite(
        photoUrl: String,
        isFavorite: Boolean,
    ) {
        val normalizedUrl = photoUrl.normalizePhotoUrl()

        if (isFavorite) {
            workLocalRepository.addFavorite(
                WorkLocalItem(
                    url = normalizedUrl,
                    isFavorite = true,
                )
            )
        } else {
            workLocalRepository.removeFavorite(normalizedUrl)
        }
    }

    fun clearCache() {
        Log.d(TAG, "Clearing photo cache. Previous size=${cachedIds.size}")
        cachedIds.clear()
    }

    fun updateTerm() {
        val oldTerm = currentTerm
        currentTerm = feedTerms
            .filter { it != oldTerm }
            .random()
        Log.d(TAG, "Updated feed term: $oldTerm -> $currentTerm")
    }
}