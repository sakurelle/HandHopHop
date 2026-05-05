package ru.handhophop.core.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import ru.handhophop.core.design.ButtonState.Size
import ru.handhophop.design.R


@Composable
internal fun TextButton(
    state: TextButtonState,
    onClick: () -> Unit,
) {
    val text = dimensionResource(R.dimen.button_text_main).value.sp
    HandHopHopButton(
        onClick = onClick,
        isActive = state.isActive,
        size = state.size,
        textColor = state.textColor,
        buttonColor = state.buttonColor,
    ) { contentColor ->
        Text(
            modifier = if (state.size == Size.FIX) {
                Modifier
                    .fillMaxWidth()
            } else {
                Modifier
            },
            text = state.text,
            fontSize = text,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
@Preview(showSystemUi = true)
fun ButtonPreview() {
    val stateButton = TextButtonState(
        stringResource(R.string.Download),
        size = Size.FIX,
        isActive = true,
        textColor = ButtonState.Color.Button,
        buttonColor = ButtonState.Color.BottomBar
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
