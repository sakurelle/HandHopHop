package ru.handhophop.core.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WallhavenResponse(
    @SerialName("data")
    val data: List<WallhavenPhoto>,
    @SerialName("meta")
    val meta: Meta? = null,
) {
    @Serializable
    data class Meta(
        @SerialName("current_page")
        val currentPage: Int,
        @SerialName("last_page")
        val lastPage: Int,
        @SerialName("per_page")
        val perPage: Int,
        @SerialName("total")
        val total: Int,
        @SerialName("query")
        val query: String? = null,
        @SerialName("seed")
        val seed: String? = null,
    )
}

@Serializable
data class WallhavenPhoto(
    @SerialName("id")
    val id: String,
    @SerialName("url")
    val url: String,
    @SerialName("short_url")
    val shortUrl: String? = null,
    @SerialName("views")
    val views: Int? = null,
    @SerialName("favorites")
    val favorites: Int? = null,
    @SerialName("source")
    val source: String? = null,
    @SerialName("purity")
    val purity: String,
    @SerialName("category")
    val category: String,
    @SerialName("dimension_x")
    val dimensionX: Int,
    @SerialName("dimension_y")
    val dimensionY: Int,
    @SerialName("resolution")
    val resolution: String,
    @SerialName("ratio")
    val ratio: String,
    @SerialName("file_size")
    val fileSize: Long? = null,
    @SerialName("file_type")
    val fileType: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("colors")
    val colors: List<String>? = null,
    @SerialName("path")
    val path: String,
    @SerialName("thumbs")
    val thumbs: ThumbsResponse? = null,
) {
    @Serializable
    data class ThumbsResponse(
        @SerialName("large")
        val large: String,
        @SerialName("original")
        val original: String? = null,
        @SerialName("small")
        val small: String,
    )
}
