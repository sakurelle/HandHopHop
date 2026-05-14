package ru.handhophop.core.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.foundation.isSystemInDarkTheme

val LocalDesignColors = compositionLocalOf { DefaultDesignColors }
val LocalDesignDimensions = compositionLocalOf { DefaultDesignDimensions }
val LocalIsDarkTheme = compositionLocalOf { false }

@Composable
fun ProvideDesignSystem(
    colors: DesignColors = DefaultDesignColors,
    dimensions: DesignDimensions = DefaultDesignDimensions,
    isDarkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalDesignColors provides colors,
        LocalDesignDimensions provides dimensions,
        LocalIsDarkTheme provides isDarkTheme,
        content = content,
    )
}

object HandHopHopDesignSystem {
    val colors: DesignColors
        @Composable
        @ReadOnlyComposable
        get() = LocalDesignColors.current

    val dimensions: DesignDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalDesignDimensions.current

    val isDarkTheme: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalIsDarkTheme.current
}

@Composable
fun HandHopHopDesignTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    ProvideDesignSystem(
        colors = if (isDarkTheme) {
            HandHopHopDesignColorScheme.dark
        } else {
            HandHopHopDesignColorScheme.light
        },
        dimensions = DefaultDesignDimensions,
        isDarkTheme = isDarkTheme,
        content = content,
    )
}
