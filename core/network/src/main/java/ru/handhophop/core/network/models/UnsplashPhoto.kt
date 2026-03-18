package ru.handhophop.core.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UnsplashPhoto(
    @SerialName("id")
    val id: String,
    @SerialName("urls")
    val urls: UrlResponse
) {
    @Serializable
    data class UrlResponse(
        @SerialName("regular")
        val regular: String
    )
}