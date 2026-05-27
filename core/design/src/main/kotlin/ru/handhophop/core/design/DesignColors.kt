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
    val error: Color = Color.Unspecified,
    val onError: Color = Color.Unspecified,
    val shimmerBase: Color = Color.Unspecified,
    val shimmerHighlight: Color = Color.Unspecified,
    val imageOverlay: Color = Color.Unspecified,
    val onImage: Color = Color.Unspecified,
    val onImageTrack: Color = Color.Unspecified,
    val favoriteAccent: Color = Color.Unspecified,
) {
    val background: Color
        get() = surfaceSoft

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
        get() = textAndIcons.copy(alpha = 0.72f)

    val onPrimaryAction: Color
        get() = notWhite

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
    error = Color(0xFF9F4B4B),
    onError = Color(0xFFFBFBFB),
    shimmerBase = Color(0xFFE4D7CE),
    shimmerHighlight = Color(0xFFF7EEE7),
    imageOverlay = Color(0x8C000000),
    onImage = Color(0xFFFFFFFF),
    onImageTrack = Color(0x59FFFFFF),
    favoriteAccent = Color(0xFFFF5A5F),
)

private val DarkDesignColors = DesignColors(
    notWhite = Color(0xFFF7EDE3),
    selection = Color(0xFF5B412E),
    lightBrown = Color(0xFF644933),
    button = Color(0xFF86684D),
    textAndIcons = Color(0xFFF1E4D8),

    surfaceSoft = Color(0xFF2A1E16),
    outline = Color(0xFF8F6E52),
    error = Color(0xFFC78778),
    onError = Color(0xFF211915),
    shimmerBase = Color(0xFF3B2B21),
    shimmerHighlight = Color(0xFF6B503B),
    imageOverlay = Color(0xA2140F0C),
    onImage = Color(0xFFF7EDE3),
    onImageTrack = Color(0x66F7EDE3),
    favoriteAccent = Color(0xFFFF7A7D),
)

internal val HandHopHopDesignColorScheme = DesignColorScheme(
    light = LightDesignColors,
    dark = DarkDesignColors,
)

internal val DefaultDesignColors = HandHopHopDesignColorScheme.light
