package ru.handhophop.core.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FreepikResponse(
    @SerialName("data")
    val data: List<FreepikPhoto>
)

@Serializable
data class FreepikPhoto(
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String? = null,
    @SerialName("image")
    val image: ImageResponse
) {
    @Serializable
    data class ImageResponse(
        @SerialName("source")
        val source: SourceResponse
    )

    @Serializable
    data class SourceResponse(
        @SerialName("url")
        val url: String
    )
}