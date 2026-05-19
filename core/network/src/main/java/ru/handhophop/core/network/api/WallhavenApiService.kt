package ru.handhophop.core.network.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.handhophop.core.network.models.WallhavenPhoto
import ru.handhophop.core.network.models.WallhavenResponse

interface WallhavenApiService {
    @GET("v1/search")
    suspend fun searchWallpapers(
        @Query("q") query: String? = null,
        @Query("categories") categories: String = "100",
        @Query("purity") purity: String = "100",
        @Query("sorting") sorting: String = "random",
        @Query("topRange") topRange: String? = null,
        @Query("ratios") ratios: String? = null,
        @Query("resolutions") resolutions: String? = null,
        @Query("colors") colors: String? = null,
        @Query("page") page: Int = 1,
    ): WallhavenResponse

    @GET("v1/w/{id}")
    suspend fun getWallpaper(
        @Path("id") id: String,
    ): WallhavenPhoto

    @GET("v1/search")
    suspend fun getRandomWallpapers(
        @Query("q") query: String? = null,
        @Query("categories") categories: String = "100",
        @Query("purity") purity: String = "100",
        @Query("sorting") sorting: String = "random",
        @Query("ratios") ratios: String? = null,
        @Query("colors") colors: String? = null,
        @Query("page") page: Int = 1,
    ): WallhavenResponse
}
