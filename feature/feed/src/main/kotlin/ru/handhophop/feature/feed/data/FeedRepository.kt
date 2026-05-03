package ru.handhophop.feature.feed.data

import android.util.Log
import ru.handhophop.core.network.api.FreepikApiService
import ru.handhophop.core.network.models.FreepikPhoto

internal class FeedRepository(
    private val apiService: FreepikApiService
) {
    private companion object {
        const val TAG = "FeedRepository"
    }

    private val recommendedFilters = listOf("sea", "mountains")
    private val cachedIds = mutableSetOf<String>()

    suspend fun getPhotos(
        page: Int = 1,
        count: Int = 10,
        orientationId: Int = 0,
        colorId: Int = 0,
        aiId: Int = 0
    ): Result<List<FreepikPhoto>> {
        return runCatching {
            val colorParam = when (colorId) {
                0  -> null
                1  -> "black"
                2  -> "blue"
                3  -> "gray"
                4  -> "green"
                5  -> "orange"
                6  -> "red"
                7  -> "white"
                8  -> "yellow"
                9  -> "purple"
                10 -> "cyan"
                11 -> "pink"
                else -> null
            }
            val landscape  = if (orientationId == 1) 1 else null
            val portrait   = if (orientationId == 2) 1 else null
            val square     = if (orientationId == 3) 1 else null
            val panoramic  = if (orientationId == 4) 1 else null
            val aiOnly     = if (aiId == 1) 1 else null
            val aiExcluded = if (aiId == 2) 1 else null

            val photos = apiService.getRandomPhotos(
                page = page,
                limit = count,
                color = colorParam,
                landscape = landscape,
                portrait = portrait,
                square = square,
                panoramic = panoramic,
                aiOnly = aiOnly,
                aiExcluded = aiExcluded
            ).data
            val unique = photos.filter { it.id.toString() !in cachedIds }
            cachedIds.addAll(unique.map { it.id.toString() })
            Log.d(TAG, "Received photos: total=${photos.size}, unique=${unique.size}")
            unique
        }.onFailure { error ->
            Log.e(TAG, "Failed to load photos for page=$page", error)
        }
    }

    suspend fun getRecommendedPhotos(): Result<List<FreepikPhoto>> {
        val filter = recommendedFilters.random()
        return runCatching {
            Log.d(TAG, "Requesting recommended photos: term=$filter")
            val photos = apiService.getRandomPhotos(limit = 5, term = filter).data
            Log.d(TAG, "Received recommended photos: term=$filter, count=${photos.size}")
            photos
        }.onFailure { error ->
            Log.e(TAG, "Failed to load recommended photos for term=$filter", error)
        }
    }

    fun clearCache() {
        Log.d(TAG, "Clearing photo cache. Previous size=${cachedIds.size}")
        cachedIds.clear()
    }
}