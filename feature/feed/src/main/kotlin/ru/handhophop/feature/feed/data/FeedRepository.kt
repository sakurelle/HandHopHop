package ru.handhophop.feature.feed.data

import ru.handhophop.core.network.api.FreepikApiService
import ru.handhophop.core.network.models.FreepikPhoto

internal class FeedRepository(
    private val apiService: FreepikApiService
) {
    private val recommendedFilters = listOf("sea", "mountains")
    private val cachedIds = mutableSetOf<String>()

    suspend fun getPhotos(page: Int = 1, count: Int = 10): Result<List<FreepikPhoto>>{
        return runCatching {
            val photos = apiService.getRandomPhotos(page = page, limit = count).data
            val unique = photos.filter { it.id.toString() !in cachedIds }
            cachedIds.addAll(unique.map{it.id.toString()})
            unique
        }
    }

    suspend fun getRecommendedPhotos(): Result<List<FreepikPhoto>> {
        val filter = recommendedFilters.random()
        return runCatching { apiService.getRandomPhotos(limit = 5, term = filter).data }
    }

    fun clearCache() {
        cachedIds.clear()
    }
}