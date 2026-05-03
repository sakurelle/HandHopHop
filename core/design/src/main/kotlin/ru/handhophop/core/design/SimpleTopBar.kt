package ru.handhophop.core.design

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.handhophop.design.R

@Composable
fun SimpleTopBar(
    modifier: Modifier = Modifier,
    state: TopBarState,
    onClickRight: () -> Unit,
    onClickLeft: () -> Unit,
) {
    val topBarStr = dimensionResource(R.dimen.top_bar_str).value.sp
    val bottomPadding = dimensionResource(R.dimen.top_bar_padding)

    val topBarBackground = colorResource(R.color.main_color)
    val textColor = colorResource(R.color.black)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        state.leftIconRes?.let {
            Button(
                modifier = Modifier,
                onClick = onClickLeft,
                colors = ButtonDefaults.buttonColors(
                    containerColor = topBarBackground
                ),
            ) {
                Icon(
                    painter = painterResource(state.leftIconRes),
                    contentDescription = null,
                    tint = textColor
                )
            }
        }
        state.titleRes?.let {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = 16.dp,
                        bottom = bottomPadding
                    ),
                text = stringResource(state.titleRes),
                color = textColor,
                fontSize = topBarStr,
                textAlign = TextAlign.Left
            )
        }
        state.rightIconRes?.let {
            Button(
                modifier = Modifier,
                onClick = onClickRight,
                colors = ButtonDefaults.buttonColors(
                    containerColor = topBarBackground
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    painter = painterResource(state.rightIconRes),
                    contentDescription = null,
                    tint = textColor
                )
            }
        }
    }
}
