package ru.handhophop.core.network.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.handhophop.core.network.models.FreepikResponse

interface FreepikApiService {
    @GET("v1/resources")
    suspend fun getRandomPhotos(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("term") term: String? = null,
        @Query("filters[content_type][photo]") photo: Int? = null,
        @Query(value = "filters%5Bcolor%5D", encoded = true) color: String? = null,
        @Query("filters[orientation][landscape]") landscape: Int? = null,
        @Query("filters[orientation][portrait]") portrait: Int? = null,
        @Query("filters[orientation][square]") square: Int? = null,
        @Query("filters[orientation][panoramic]") panoramic: Int? = null,
        @Query("filters[ai-generated][only]") aiOnly: Int? = null,
        @Query("filters[ai-generated][excluded]") aiExcluded: Int? = null,
    ): FreepikResponse
}