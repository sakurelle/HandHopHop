package ru.handhophop.core.network.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.handhophop.core.network.models.WallhavenPhoto
import ru.handhophop.core.network.models.WallhavenResponse

interface WallhavenApiService {

    /**
     * Search wallpapers
     * @param q Search query string
     * @param categories Category bits: 100 (General), 010 (Anime), 001 (People). Combine as 111 for all
     * @param purity Purity bits: 100 (SFW), 010 (Sketchy), 001 (NSFW). NSFW requires API key
     * @param sorting Sorting: relevance, random, date_added, views, favorites, toplist
     * @param topRange Time range for toplist: 1d, 3d, 1w, 1M, 3M, 6M, 1y
     * @param ratios Aspect ratios, e.g., 16x9, landscape
     * @param resolutions Minimum resolution, e.g., 1920x1080
     * @param colors Color filter (comma-separated hex values without #)
     * @param page Page number (24 results per page by default)
     */
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
        @Query("page") page: Int = 1
    ): WallhavenResponse

    /**
     * Get wallpaper details by ID
     * @param id Wallpaper ID
     */
    @GET("v1/w/{id}")
    suspend fun getWallpaper(
        @Path("id") id: String
    ): WallhavenPhoto

    /**
     * Get random wallpapers (uses search with random sorting)
     * @param count Number of wallpapers to return (will calculate pages needed)
     * @param query Search query string
     * @param ratios Aspect ratios filter
     * @param colors Color filter
     */
    @GET("v1/search")
    suspend fun getRandomWallpapers(
        @Query("q") query: String? = null,
        @Query("categories") categories: String = "100",
        @Query("purity") purity: String = "100",
        @Query("sorting") sorting: String = "random",
        @Query("ratios") ratios: String? = null,
        @Query("colors") colors: String? = null,
        @Query("page") page: Int = 1
    ): WallhavenResponse
}
