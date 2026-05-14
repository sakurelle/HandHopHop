package ru.handhophop.core.design

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    val padding = dimensionResource(R.dimen.top_bar_padding)
    val buttonWidth = 40.dp
    val iconSize = 24.dp

    val colors = HandHopHopDesignSystem.colors
    val topBarBackground = colors.topBar
    val textColor = colors.textPrimary


    Row(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = padding / 2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        state.leftIconRes?.let {
            Button(
                modifier = Modifier
                    .width(buttonWidth)
                    .padding(0.dp),
                onClick = onClickLeft,
                colors = ButtonDefaults.buttonColors(
                    containerColor = topBarBackground
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    modifier = Modifier
                        .size(iconSize)
                        .padding(0.dp),
                    painter = painterResource(state.leftIconRes),
                    contentDescription = null,
                    tint = textColor
                )
            }
        }
        state.titleRes?.let {
            Text(
                modifier = Modifier
                    .weight(1f),
                text = stringResource(state.titleRes),
                color = textColor,
                fontSize = topBarStr,
                textAlign = TextAlign.Left
            )
        }
        state.projectName?.let {
            Text(
                modifier = Modifier
                    .weight(1f),
                text = state.projectName,
                color = textColor,
                fontSize = topBarStr,
                textAlign = TextAlign.Left
            )
        }
        state.rightIconRes?.let {
            Button(
                modifier = Modifier
                    .width(buttonWidth)
                    .padding(0.dp),
                onClick = onClickRight,
                colors = ButtonDefaults.buttonColors(
                    containerColor = topBarBackground
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    modifier = Modifier
                        .size(iconSize)
                        .padding(0.dp),
                    painter = painterResource(state.rightIconRes),
                    contentDescription = null,
                    tint = textColor
                )

            }
        }
    }
}


@Composable
@Preview(showSystemUi = true)
fun ExposableTopBar1(
) {
    SimpleTopBar(
        modifier = Modifier,
        TopBarState(R.string.Download, R.drawable.arrow, null),
        {},
        {}
    )
}
