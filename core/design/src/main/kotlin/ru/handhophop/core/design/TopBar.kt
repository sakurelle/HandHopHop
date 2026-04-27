package ru.handhophop.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.handhophop.design.R

@Composable
fun TopBar(
    state: TopBarState,
    onClickRight: () -> Unit,
    onClickLeft: () -> Unit,
) {
    val height = dimensionResource(R.dimen.top_bar_height)
    val radius = dimensionResource(R.dimen.main_radius)
    val topBarBackground = colorResource(R.color.main_color)
    val padding = dimensionResource(R.dimen.top_bar_padding)


    Box(
        modifier = Modifier
            .height(height)
            .fillMaxWidth()
            .background(
                topBarBackground,
                shape = RoundedCornerShape(
                    bottomEnd = radius,
                    bottomStart = radius
                )
            )
            .padding(padding)
            ,
        contentAlignment = Alignment.Center
    ) {
        SimpleTopBar(
            modifier = Modifier,
            state = state,
            onClickRight = onClickRight,
            onClickLeft = onClickLeft
        )
    }
}