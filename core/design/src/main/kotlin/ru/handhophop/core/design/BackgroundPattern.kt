package ru.handhophop.core.design

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import ru.handhophop.design.R


@Composable
fun BackgroundPattern() {
    val alpha = integerResource(R.integer.bg_pattern_alpha_percent) / 100f
    val patternRes = if (HandHopHopDesignSystem.isDarkTheme) {
        R.drawable.dark_theme
    } else {
        R.drawable.bg_pattern
    }

    Image(
        painter = painterResource(patternRes),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
        alpha = alpha
    )
}
