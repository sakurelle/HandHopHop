package ru.handhophop.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.xr.compose.testing.toDp
import ru.handhophop.design.R

@Composable
fun ExposableTopBar(
    state: TopBarState,
    onChanged: (isExposed: Boolean) -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val height = dimensionResource(R.dimen.filter_top_bar_height)
    val radius = dimensionResource(R.dimen.main_radius)
    val padding = dimensionResource(R.dimen.top_bar_padding)

    val topBarBackground = colorResource(R.color.main_color)

    var isExposed by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val navBarHeight = with(density) {
        WindowInsets.navigationBars.getTop(density).toDp()
    }

    Box(
        modifier = Modifier
            .background(
                topBarBackground,
                shape = RoundedCornerShape(
                    bottomEnd = radius,
                    bottomStart = radius
                )
            )
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(padding)
                .wrapContentHeight()
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SimpleTopBar(
                    state = state,
                    onClickRight = {
                        isExposed = !isExposed
                        onChanged(isExposed)
                    },
                    onClickLeft = { Unit }
                )
            }
            if (isExposed) {
                content()
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun ExposableTopBar11(
) {

    ExposableTopBar(
        TopBarState(R.string.Download, null, null),
        onChanged = {},
        content = {}
    )
}


