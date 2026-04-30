package ru.handhophop.core.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

val LocalDesignColors = compositionLocalOf { DefaultDesignColors }
val LocalDesignDimensions = compositionLocalOf { DefaultDesignDimensions }

@Composable
fun ProvideDesignSystem(
    colors: DesignColors = DefaultDesignColors,
    dimensions: DesignDimensions = DefaultDesignDimensions,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalDesignColors provides colors,
        LocalDesignDimensions provides dimensions,
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
}
