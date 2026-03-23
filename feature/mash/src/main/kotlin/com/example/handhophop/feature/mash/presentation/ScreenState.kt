package com.example.handhophop.feature.mash.presentation

import androidx.compose.runtime.Immutable

@Immutable
internal data class ScreenState(
    val route: Route
)

internal enum class Route {
    MASH,
    FEED,
    BOOKMARK,
    PROFILE
}