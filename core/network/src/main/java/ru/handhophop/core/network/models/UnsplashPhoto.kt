package ru.handhophop.core.network.models

import kotlinx.serialization.Serializable

@Serializable
data class UnsplashPhoto(
    val id: String
) {
    @Serializable
    data class Urls(
        val regular: String
    )
}