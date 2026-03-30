package ru.handhophop.core.design

import androidx.compose.runtime.Immutable

@Immutable
data class ScreenState(
    val route: Route
)

enum class Route {
    MASH,
    FEED,
    BOOKMARK,
    PROFILE
}