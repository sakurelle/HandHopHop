package com.example.handhophop.feature.mash.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import ru.handhophop.feature.mash.R

@Composable
internal fun BackgroundPattern() {
    val alpha = runCatching { dimensionResource(R.dimen.bg_pattern_alpha).value }.getOrElse { 0.22f }

    Image(
        painter = painterResource(R.drawable.bg_pattern),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
        alpha = alpha
    )
}