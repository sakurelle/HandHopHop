package ru.handhophop.core.design

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    val radius = dimensionResource(R.dimen.main_radius)
    val height = dimensionResource(R.dimen.button_height)
    val padding = dimensionResource(R.dimen.main_padding)

    val text = dimensionResource(R.dimen.button_text_main).value.sp

    val buttonColor = state.buttonColor.getColor()
    val rowColor = state.textColor.getColor()



    Button(
        modifier = if (state.size == Size.FIX) {
            Modifier
                .fillMaxWidth()
                .padding(
                    start = padding,
                    end = padding
                )
                .height(height)
        } else
            Modifier
                .wrapContentWidth()
                .height(height),
        onClick = onClick,
        shape = RoundedCornerShape(radius),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        )
    ) {
        Row(
            modifier = Modifier
        ) {
            Text(
                modifier = if (state.size == Size.FIX) {
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                } else {
                    Modifier
                },
                text = state.text,
                fontSize = text,
                color = if (state.isActive) rowColor else rowColor.copy(alpha = 0.5f),
                textAlign = TextAlign.Start
            )
            state.icon?.let {
                Icon(
                    modifier = Modifier,
                    painter = state.icon,
                    contentDescription = null,
                    tint = if (state.isActive) rowColor else rowColor.copy(alpha = 0.5f),
                )
            }
        }
    }
}