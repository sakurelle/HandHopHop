package ru.handhophop.feature.bookmark.presentation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ru.handhophop.feature.bookmark.R

@Composable
fun BookmarkFilterOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(dimensionResource(R.dimen.bookmark_grid_card_corner_radius)),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                colorResource(R.color.bookmark_filter_selected_background)
            } else {
                colorResource(R.color.bookmark_filter_unselected_background)
            },
            contentColor = if (selected) {
                colorResource(R.color.bookmark_filter_selected_text)
            } else {
                colorResource(R.color.bookmark_filter_unselected_text)
            }
        ),
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            fontSize = dimensionResource(R.dimen.bookmark_filter_option_text_size).value.sp
        )
    }
}