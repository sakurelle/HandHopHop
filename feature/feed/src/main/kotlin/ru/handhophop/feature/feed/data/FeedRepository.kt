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

    suspend fun getPhotos(page: Int = 1, count: Int = 10): Result<List<FreepikPhoto>> {
        return runCatching {
            Log.d(TAG, "Requesting photos: page=$page, count=$count, cachedIds=${cachedIds.size}")
            val photos = apiService.getRandomPhotos(page = page, limit = count).data
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
