package ru.handhophop.core.design

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ru.handhophop.core.design.ButtonState.Size
import ru.handhophop.design.R

@Composable
internal fun IconButton(
    state: IconButtonState,
    onClick: () -> Unit,
) {
    val padding = dimensionResource(R.dimen.main_padding)
    val text = dimensionResource(R.dimen.button_text_main).value.sp
    HandHopHopButton(
        onClick = onClick,
        modifier = if (state.size == Size.FIX) {
            Modifier
                .fillMaxWidth()
                .padding(
                    start = padding,
                    end = padding,
                )
        } else {
            Modifier
        },
        isActive = state.isActive,
        size = state.size,
        textColor = state.textColor,
        buttonColor = state.buttonColor,
    ) { contentColor ->
        Row(
            modifier = if (state.size == Size.FIX) {
                Modifier.fillMaxWidth()
            } else {
                Modifier
            },
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                modifier = Modifier,
                text = state.text,
                fontSize = text,
                color = contentColor,
                textAlign = TextAlign.Start,
            )
            state.icon?.let {
                Icon(
                    modifier = Modifier,
                    painter = state.icon,
                    contentDescription = null,
                    tint = contentColor,
                )
            }
        }
    }
}
