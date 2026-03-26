package ru.handhophop.core.network.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.handhophop.core.network.models.UnsplashPhoto

interface UnsplashApiService {
    @GET("photos/random")
    suspend fun getRandomPhotos(
        @Query("count") count: Int = 10
    ): List<UnsplashPhoto>
}