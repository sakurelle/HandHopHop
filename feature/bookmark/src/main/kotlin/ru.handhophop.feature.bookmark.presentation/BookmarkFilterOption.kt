package ru.handhophop.feature.bookmark.presentation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.design.R as DesignR

@Composable
fun BookmarkFilterOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = HandHopHopDesignSystem.colors
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(dimensionResource(DesignR.dimen.bookmark_grid_card_corner_radius)),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                colors.primaryAction
            } else {
                colors.bottomBar
            },
            contentColor = if (selected) {
                colors.onPrimaryAction
            } else {
                colors.textPrimary
            }
        ),
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            fontSize = dimensionResource(DesignR.dimen.bookmark_filter_option_text_size).value.sp
        )
    }
}
