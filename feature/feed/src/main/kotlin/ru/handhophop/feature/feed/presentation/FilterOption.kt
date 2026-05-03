package ru.handhophop.feature.feed.presentation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ru.handhophop.design.R

@Composable
fun FilterOption(
    filterOptionState: FilterOptionState,
    filterCondition: (id: Int) -> Unit
) {
    val height = dimensionResource(R.dimen.filter_option_height)
    val fontSize = dimensionResource(R.dimen.filter_option_text_size).value.sp
    val radius = dimensionResource(R.dimen.filter_option_radius)

    val color =
        if (filterOptionState.isSelected)
            colorResource(
                R.color.button
            )
        else
            colorResource(
            R.color.bottom_bar
        )
    val textColor =colorResource(R.color.white)

    Button(
        modifier = Modifier
            .height(height = height)
            .wrapContentWidth(),
        shape = RoundedCornerShape(radius),
        onClick = { filterCondition(filterOptionState.id) },
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        )
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(filterOptionState.textRes),
            textAlign = TextAlign.Center,
            color = textColor,
            fontSize = fontSize
        )
    }
}