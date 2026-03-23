package com.example.handhophop.feature.mash.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.handhophop.feature.mash.presentation.ButtonState.Size
import ru.handhophop.feature.mash.R


@Composable
internal fun TextButton(
    state: TextButtonState,
    onClick: () -> Unit,
) {
    val radius = dimensionResource(R.dimen.main_radius)
    val height = dimensionResource(R.dimen.button_height)
    val padding = dimensionResource(R.dimen.main_padding)

    val text = dimensionResource(R.dimen.button_text_main).value.sp

    val buttonColor = state.buttonColor.getColor()
    val rowColor = state.textColor.getColor()



    Button(
        modifier = if (state.size == Size.MAX) {
            Modifier
                .fillMaxWidth()
                .padding(
                    start = padding,
                    end = padding
                )
                .height(height)
                .border(
                    width = 1.dp,
                    color = buttonColor,
                    shape = RoundedCornerShape(radius)
                )

        } else
            Modifier
                .wrapContentWidth()
                .height(height)
                .border(
                    width = 1.dp,
                    color = buttonColor,
                    shape = RoundedCornerShape(radius)
                ),
        onClick = onClick,
        shape = RoundedCornerShape(radius),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        )
    ) {
        Text(
            modifier = if (state.size == Size.MAX) {
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            } else {
                Modifier
            },
            text = state.text,
            fontSize = text,
            color = if (state.isActive) rowColor else rowColor.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
@Preview(showSystemUi = true)
fun ButtonPreview() {
    val stateButton = TextButtonState(
        stringResource(R.string.Download),
        size = Size.MAX,
        isActive = true,
        textColor = ButtonState.Color.Default,
        buttonColor = ButtonState.Color.Accent
    )
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            state = stateButton,
            onClick = {/*TODO*/ }
        )
    }

}