package ru.handhophop.feature.feed.data

import android.util.Log
import ru.handhophop.core.network.api.FreepikApiService
import ru.handhophop.core.network.models.FreepikPhoto
import ru.handhophop.core.system.database.work.WorkLocalItem
import ru.handhophop.core.system.database.work.WorkLocalRepository

internal class FeedRepository(
    private val apiService: FreepikApiService,
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
        "birds",
    )

    private var currentTerm: String = feedTerms.random()
    private val cachedIds = mutableSetOf<String>()

    private fun String.normalizePhotoUrl(): String {
        return replace("http://", "https://")
    }

    private suspend fun fillPhotosWithLocalData(
        photos: List<FreepikPhoto>,
    ): List<FeedPhotoModel> {
        val normalizedPhotos = photos.map { photo ->
            photo to photo.image.source.url.normalizePhotoUrl()
        }

        val worksByUrl = workLocalRepository
            .getFeedMetaByUrls(normalizedPhotos.map { it.second })
            .associateBy { it.url.orEmpty().normalizePhotoUrl() }

        return normalizedPhotos.map { (photo, normalizedUrl) ->
            val localWork = worksByUrl[normalizedUrl]
            val hasStarted = localWork?.isStarted == true

            FeedPhotoModel(
                id = photo.id.toString(),
                photoUrl = normalizedUrl,
                isBookmarked = localWork?.isFavorite == true,
                isStarted = hasStarted,
                progressPercentage = if (hasStarted) {
                    localWork?.percentage ?: 0
                } else {
                    0
                },
            )
        }
    }

    suspend fun getPhotos(
        page: Int = 1,
        count: Int = 10,
        orientationId: Int = 0,
        colorId: Int = 0,
        aiId: Int = 0,
    ): Result<List<FeedPhotoModel>> {
        return runCatching {
            val colorParam = when (colorId) {
                0 -> null
                1 -> "black"
                2 -> "blue"
                3 -> "gray"
                4 -> "green"
                5 -> "orange"
                6 -> "red"
                7 -> "white"
                8 -> "yellow"
                9 -> "purple"
                10 -> "cyan"
                11 -> "pink"
                else -> null
            }

            val landscape = if (orientationId == 1) 1 else null
            val portrait = if (orientationId == 2) 1 else null
            val square = if (orientationId == 3) 1 else null
            val panoramic = if (orientationId == 4) 1 else null
            val aiOnly = if (aiId == 1) 1 else null
            val aiExcluded = if (aiId == 2) 1 else null

            val photos = apiService.getRandomPhotos(
                page = page,
                limit = count,
                term = currentTerm,
                photo = 1,
                color = colorParam,
                landscape = landscape,
                portrait = portrait,
                square = square,
                panoramic = panoramic,
                aiOnly = aiOnly,
                aiExcluded = aiExcluded,
            ).data

            val unique = photos.filter { it.id.toString() !in cachedIds }
            cachedIds.addAll(unique.map { it.id.toString() })
            Log.d(TAG, "Received photos: total=${photos.size}, unique=${unique.size}, filter=$currentTerm")
            fillPhotosWithLocalData(unique)
        }.onFailure { error ->
            Log.e(TAG, "Failed to load photos for page=$page", error)
        }
    }

    suspend fun getRecommendedPhotos(): Result<List<FeedPhotoModel>> {
        return runCatching {
            Log.d(TAG, "Requesting recommended photos")
            val photos = apiService.getRandomPhotos(
                limit = 5,
                photo = 1,
            ).data
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
                ),
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
