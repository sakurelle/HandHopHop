package com.example.handhophop.feature.mash.presentation

import kotlinx.coroutines.flow.StateFlow

internal data class MashUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val reachedEnd: Boolean = false,
    val isSquareFilterEnabled: Boolean = false,
    val items: ImageItem? = null,
)


internal data class ImageItem(
    val id: String,
    val imageUrl: String,
    val aspectRatio: Float,
    val author: String = ""
)