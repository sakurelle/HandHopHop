package com.example.handhophop.feature.mash.presentation


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ru.handhophop.feature.mash.R

@Composable
internal fun DownloadButton(state: ButtonState){
    val textButton = stringResource(R.string.Download)


    val radius = dimensionResource(R.dimen.main_radius)
    val height = dimensionResource(R.dimen.button_height)
    val padding = dimensionResource(R.dimen.main_padding)

    val text = dimensionResource(R.dimen.button_text_main).value.sp

    val buttonColor = colorResource(R.color.main_color)
    val rowColor = colorResource(R.color.button)


    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = padding,
                end = padding
            )
            .height(height),
        onClick = { /*TODO*/ },
        shape = RoundedCornerShape(radius),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        )
    ) {
        Row(
            modifier = Modifier
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                text = textButton,
                fontSize = text,
                color =if(state.haveImage)rowColor else  rowColor.copy(alpha = 0.5f),
                textAlign = TextAlign.Start

            )
            Icon(
                modifier = Modifier,
                painter = painterResource(R.drawable.arrow),
                contentDescription = null,
                tint =if(state.haveImage)rowColor else  rowColor.copy(alpha = 0.5f)
            )
        }
    }
}



