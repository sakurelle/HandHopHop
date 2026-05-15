package ru.handhophop.core.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

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
    val designColors = if (isDarkTheme) {
        HandHopHopDesignColorScheme.dark
    } else {
        HandHopHopDesignColorScheme.light
    }
    ProvideDesignSystem(
        colors = designColors,
        dimensions = DefaultDesignDimensions,
        isDarkTheme = isDarkTheme,
    ) {
        val materialColors = if (isDarkTheme) {
            darkColorScheme(
                primary = designColors.primaryAction,
                onPrimary = designColors.onPrimaryAction,
                secondary = designColors.bottomBar,
                onSecondary = designColors.textPrimary,
                tertiary = designColors.selection,
                onTertiary = designColors.textPrimary,
                background = designColors.background,
                onBackground = designColors.textPrimary,
                surface = designColors.surface,
                onSurface = designColors.textPrimary,
                surfaceVariant = designColors.surfaceSoft,
                onSurfaceVariant = designColors.textSecondary,
                outline = designColors.outline,
                error = designColors.error,
                onError = designColors.onError,
            )
        } else {
            lightColorScheme(
                primary = designColors.primaryAction,
                onPrimary = designColors.onPrimaryAction,
                secondary = designColors.bottomBar,
                onSecondary = designColors.textPrimary,
                tertiary = designColors.selection,
                onTertiary = designColors.textPrimary,
                background = designColors.background,
                onBackground = designColors.textPrimary,
                surface = designColors.surface,
                onSurface = designColors.textPrimary,
                surfaceVariant = designColors.surfaceSoft,
                onSurfaceVariant = designColors.textSecondary,
                outline = designColors.outline,
                error = designColors.error,
                onError = designColors.onError,
            )
        }

        MaterialTheme(
            colorScheme = materialColors,
            content = content,
        )
    }
}
