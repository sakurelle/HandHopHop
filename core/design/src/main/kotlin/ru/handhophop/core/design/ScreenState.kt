package ru.handhophop.core.design

import androidx.compose.runtime.Immutable

@Immutable
data class ScreenState(
    val currentScreen: Route
)

enum class Route {
    MASH,
    FEED,
    BOOKMARK,
    SETTINGS
}
