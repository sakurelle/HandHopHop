package ru.handhophop.feature.feed.presentation

import ru.handhophop.core.network.api.UnsplashApiService
import ru.handhophop.core.network.models.UnsplashPhoto

internal class FeedRepository(
    private val apiService: UnsplashApiService
) {
    suspend fun getPhotos(count: Int = 10): Result<List<UnsplashPhoto>>{
        return try {
            val photos: List<UnsplashPhoto> = apiService.getRandomPhotos(count)
            Result.success(photos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}