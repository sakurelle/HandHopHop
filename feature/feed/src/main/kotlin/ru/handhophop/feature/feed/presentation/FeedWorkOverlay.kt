package ru.handhophop.feature.feed.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.feature.feed.R
import ru.handhophop.design.R as DesignR

@Composable
internal fun FeedProgressCircle(
    progressPercentage: Int,
    modifier: Modifier = Modifier,
) {
    val colors = HandHopHopDesignSystem.colors
    val safeProgress = remember(progressPercentage) {
        progressPercentage.coerceIn(0, 100)
    }
    val progress = remember(safeProgress) {
        safeProgress / 100f
    }

    Box(
        modifier = modifier
            .size(dimensionResource(DesignR.dimen.feed_progress_circle_size))
            .background(
                color = colors.imageOverlay,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(dimensionResource(DesignR.dimen.feed_progress_indicator_size)),
            strokeWidth = dimensionResource(DesignR.dimen.feed_progress_indicator_stroke_width),
            color = colors.onImage,
            trackColor = colors.onImageTrack,
            strokeCap = StrokeCap.Round,
        )

        Text(
            text = stringResource(R.string.feed_progress_percent, safeProgress),
            color = colors.onImage,
            fontSize = dimensionResource(DesignR.dimen.feed_progress_text_size).value.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
internal fun FeedBookmarkButton(
    isBookmarked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HandHopHopDesignSystem.colors
    val iconRes = remember(isBookmarked) {
        if (isBookmarked) {
            DesignR.drawable.ic_bookmark_filled
        } else {
            DesignR.drawable.ic_bookmark_outline
        }
    }

    val contentDescription = if (isBookmarked) {
        stringResource(R.string.feed_bookmark_remove_from_favorites)
    } else {
        stringResource(R.string.feed_bookmark_add_to_favorites)
    }

    Box(
        modifier = modifier
            .size(
                width = dimensionResource(DesignR.dimen.feed_bookmark_button_width),
                height = dimensionResource(DesignR.dimen.feed_bookmark_button_height),
            )
            .background(
                color = colors.imageOverlay,
                shape = RoundedCornerShape(
                    topStart = dimensionResource(
                        DesignR.dimen.feed_bookmark_button_corner_zero
                    ),
                    topEnd = dimensionResource(
                        DesignR.dimen.feed_bookmark_button_corner_zero
                    ),
                    bottomStart = dimensionResource(
                        DesignR.dimen.feed_bookmark_button_bottom_start_radius
                    ),
                    bottomEnd = dimensionResource(
                        DesignR.dimen.feed_bookmark_button_corner_zero
                    ),
                ),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = if (isBookmarked) {
                colors.favoriteAccent
            } else {
                colors.onImage
            },
            modifier = Modifier.size(dimensionResource(DesignR.dimen.feed_bookmark_icon_size)),
        )
    }
}
