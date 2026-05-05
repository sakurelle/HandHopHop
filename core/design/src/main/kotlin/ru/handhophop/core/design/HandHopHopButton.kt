package ru.handhophop.core.design

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import ru.handhophop.core.design.ButtonState.Size
import ru.handhophop.design.R

@Composable
fun HandHopHopButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isActive: Boolean = true,
    size: Size = Size.FIX,
    textColor: ButtonState.Color = ButtonState.Color.White,
    buttonColor: ButtonState.Color = ButtonState.Color.Button,
    borderColor: ButtonState.Color = ButtonState.Color.Button,
    content: @Composable RowScope.(contentColor: Color) -> Unit,
) {
    val radius = dimensionResource(R.dimen.main_radius)
    val height = dimensionResource(R.dimen.button_height)
    val width = dimensionResource(R.dimen.button_width)
    val border = dimensionResource(R.dimen.button_border)

    val containerColor = buttonColor.getColor()
    val outlineColor = borderColor.getColor()
    val contentColor = textColor.getColor().let { baseColor ->
        if (enabled && isActive) baseColor else baseColor.copy(alpha = 0.5f)
    }

    val sizeModifier = when (size) {
        Size.FIX -> Modifier
            .height(height)
            .width(width)

        Size.WRAPCONTENT -> Modifier
            .wrapContentWidth()
            .height(height)
    }

    Button(
        modifier = sizeModifier
            .then(modifier)
            .border(
                width = border,
                color = outlineColor,
                shape = RoundedCornerShape(radius),
            ),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(radius),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.55f),
            disabledContentColor = contentColor,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = { content(contentColor) },
        )
    }
}
