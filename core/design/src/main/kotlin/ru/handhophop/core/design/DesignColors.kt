package ru.handhophop.core.design

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class DesignColors(
    val background: Color = Color.Unspecified,
    val surface: Color = Color.Unspecified,
    val primary: Color = Color.Unspecified,
    val onPrimary: Color = Color.Unspecified,
    val accent: Color = Color.Unspecified,
)

internal val DefaultDesignColors = DesignColors()
