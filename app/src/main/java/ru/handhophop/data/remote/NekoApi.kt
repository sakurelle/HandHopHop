package ru.handhophop.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

data class NekoImageDto(
    val id: Int,
    val url: String,
    val rating: String,
    val artist_name: String? = null,
    val color_dominant: List<Int>? = null,
    val color_palette: List<List<Int>>? = null,
    val tags: List<String>? = null,
    val source_url: String? = null
)

interface NekoApi {
    @GET("images/{id}")
    suspend fun getImageById(@Path("id") id: Int): NekoImageDto
}
