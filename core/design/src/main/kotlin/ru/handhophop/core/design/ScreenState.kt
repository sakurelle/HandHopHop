package ru.handhophop.core.design

import androidx.compose.runtime.Immutable

@Immutable
data class ScreenState(
    val currentScreen: Route,
    val topBarState: TopBarState
)

enum class Route {
    MASH,
    FEED,
    BOOKMARK,
    SETTINGS
}
