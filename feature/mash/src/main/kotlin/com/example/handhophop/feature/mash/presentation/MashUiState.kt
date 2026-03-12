package com.example.handhophop.feature.mash.presentation

import androidx.compose.ui.graphics.Color

internal data class MashUiState(
   val isLoading: Boolean = false,
   val imageUrl: String? = null,
   val scheme: SchemeData? = null,
   val error: String? = null
)

internal data class SchemeData(
   val gridW: Int,
   val gridH: Int,
   val palette: List<Color>,
   val indices: IntArray
)