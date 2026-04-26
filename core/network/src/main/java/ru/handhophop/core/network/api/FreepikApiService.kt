package ru.handhophop.core.network.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.handhophop.core.network.models.FreepikResponse

interface FreepikApiService {
    @GET("v1/resources")
    suspend fun getRandomPhotos(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("term") term: String? = null
    ): FreepikResponse
}