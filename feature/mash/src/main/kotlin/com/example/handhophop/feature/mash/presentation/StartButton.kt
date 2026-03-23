package com.example.handhophop.feature.mash.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.handhophop.feature.mash.R


@Composable
internal fun StartButton(state: ButtonState){
    val textButton = stringResource(R.string.Start)

    val radius = dimensionResource(R.dimen.main_radius)
    val height = dimensionResource(R.dimen.button_height)
    val padding = dimensionResource(R.dimen.main_padding)

    val text = dimensionResource(R.dimen.button_text_main).value.sp

    val color = colorResource(R.color.button)



    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = padding,
                end = padding
            )
            .height(height),
        onClick = {/*TODO*/ },
        shape = RoundedCornerShape(radius),
        colors = ButtonDefaults.buttonColors(
            containerColor = if(state.haveImage)color else  color.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = textButton,
            fontSize = text
        )

    }
}

@Composable
@Preview(showSystemUi = true)
fun prew1(){
    val stateButton = ButtonState(true)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ){

        StartButton(state = stateButton)
    }

}