package ru.handhophop.core.design

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class DesignColors(
    val notWhite: Color = Color.Unspecified,
    val selection: Color = Color.Unspecified,
    val lightBrown: Color = Color.Unspecified,
    val button: Color = Color.Unspecified,
    val textAndIcons: Color = Color.Unspecified,
    val surfaceSoft: Color = Color.Unspecified,
    val outline: Color = Color.Unspecified,
) {
    val background: Color
        get() = notWhite

    val topBar: Color
        get() = selection

    val bottomBar: Color
        get() = lightBrown

    val surface: Color
        get() = selection

    val primaryAction: Color
        get() = button

    val textPrimary: Color
        get() = textAndIcons

    val textSecondary: Color
        get() = button

    val onPrimaryAction: Color
        get() = notWhite

    val floatingAction: Color
        get() = textAndIcons
}

@Immutable
data class DesignColorScheme(
    val light: DesignColors,
    val dark: DesignColors,
)

private val LightDesignColors = DesignColors(
    notWhite = Color(0xFFFBFBFB),
    selection = Color(0xFFDBC5B9),
    lightBrown = Color(0xFFBD9C89),
    button = Color(0xFF69564B),
    textAndIcons = Color(0xFF211915),

    surfaceSoft = Color(0xFFE9DDD5),
    outline = Color(0xFFBD9C89),
)

private val DarkDesignColors = DesignColors(
    notWhite = Color(0xFFF7EDE3),
    selection = Color(0xFF5B412E),
    lightBrown = Color(0xFF644933),
    button = Color(0xFF86684D),
    textAndIcons = Color(0xFFF1E4D8),

    surfaceSoft = Color(0xFF2A1E16),
    outline = Color(0xFF8F6E52),
)

internal val HandHopHopDesignColorScheme = DesignColorScheme(
    light = LightDesignColors,
    dark = DarkDesignColors,
)

internal val DefaultDesignColors = HandHopHopDesignColorScheme.light
