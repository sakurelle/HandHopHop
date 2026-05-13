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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.handhophop.feature.feed.R
import ru.handhophop.design.R as DesignR

@Composable
internal fun FeedProgressCircle(
    progressPercentage: Int,
    modifier: Modifier = Modifier,
) {
    val safeProgress = remember(progressPercentage) {
        progressPercentage.coerceIn(0, 100)
    }
    val progress = remember(safeProgress) {
        safeProgress / 100f
    }

    Box(
        modifier = modifier
            .size(dimensionResource(R.dimen.feed_progress_circle_size))
            .background(
                color = colorResource(R.color.feed_overlay_background),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(dimensionResource(R.dimen.feed_progress_indicator_size)),
            strokeWidth = dimensionResource(R.dimen.feed_progress_indicator_stroke_width),
            color = colorResource(R.color.feed_progress_indicator_color),
            trackColor = colorResource(R.color.feed_progress_indicator_track_color),
            strokeCap = StrokeCap.Round,
        )

        Text(
            text = stringResource(R.string.feed_progress_percent, safeProgress),
            color = colorResource(R.color.feed_progress_text_color),
            fontSize = dimensionResource(R.dimen.feed_progress_text_size).value.sp,
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
                width = dimensionResource(R.dimen.feed_bookmark_button_width),
                height = dimensionResource(R.dimen.feed_bookmark_button_height),
            )
            .background(
                color = colorResource(R.color.feed_overlay_background),
                shape = RoundedCornerShape(
                    topStart = dimensionResource(
                        R.dimen.feed_bookmark_button_corner_zero
                    ),
                    topEnd = dimensionResource(
                        R.dimen.feed_bookmark_button_corner_zero
                    ),
                    bottomStart = dimensionResource(
                        R.dimen.feed_bookmark_button_bottom_start_radius
                    ),
                    bottomEnd = dimensionResource(
                        R.dimen.feed_bookmark_button_corner_zero
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
                colorResource(R.color.feed_bookmark_selected_color)
            } else {
                colorResource(R.color.feed_bookmark_unselected_color)
            },
            modifier = Modifier.size(dimensionResource(R.dimen.feed_bookmark_icon_size)),
        )
    }
}