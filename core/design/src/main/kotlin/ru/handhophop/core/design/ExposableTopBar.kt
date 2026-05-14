package ru.handhophop.core.design

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import ru.handhophop.design.R

@Composable
fun ExposableTopBar(
    state: TopBarState,
    onChanged: (isExposed: Boolean) -> Unit,
    content: @Composable (onDismiss: () -> Unit) -> Unit
) {
    val radius = dimensionResource(R.dimen.main_radius)
    val padding = dimensionResource(R.dimen.top_bar_padding)
    val colors = HandHopHopDesignSystem.colors

    var isExposed by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .background(
                colors.topBar,
                shape = RoundedCornerShape(
                    bottomEnd = radius,
                    bottomStart = radius
                )
            )
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .wrapContentHeight()
                .padding(padding)
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
                if (isExposed) {
                    content {
                        isExposed = false
                        onChanged(false)
                    }
                }
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


