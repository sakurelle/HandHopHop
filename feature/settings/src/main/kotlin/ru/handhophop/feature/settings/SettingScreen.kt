package ru.handhophop.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import ru.handhophop.core.design.ButtonState
import ru.handhophop.core.design.HandHopHopButton
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.core.design.TopBar
import ru.handhophop.core.design.TopBarState
import ru.handhophop.design.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onChangeTheme: (isActive: Boolean) -> Unit,
    onClick: () -> Unit,
) {
    var isChecked by remember { mutableStateOf(false) }
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions

    val fontSize = dimensionResource(DesignR.dimen.font_size).value.sp
    val radius = dimensionResource(DesignR.dimen.radius)
    val border = dimensionResource(DesignR.dimen.border)
    val width = dimensionResource(DesignR.dimen.width)
    val heightBlock = dimensionResource(DesignR.dimen.height_block)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar(
                state = TopBarState(
                    R.string.setting,
                    null,
                    null,
                ),
                onClickRight = { Unit },
                onClickLeft = { Unit },
            )

            Box(
                modifier = Modifier
                    .padding(top = dimensions.md)
                    .width(width)
                    .height(heightBlock)
                    .border(
                        border,
                        colors.primaryAction,
                        shape = RoundedCornerShape(radius),
                    )
                    .background(
                        color = colors.topBar,
                        shape = RoundedCornerShape(radius),
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(dimensions.md),
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = dimensions.md),
                        text = stringResource(R.string.dark_theme),
                        fontSize = fontSize,
                        color = colors.textPrimary,
                    )
                    Switch(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            isChecked = checked
                            onChangeTheme(checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.background,
                            checkedTrackColor = colors.primaryAction,
                            uncheckedThumbColor = colors.background,
                            uncheckedTrackColor = colors.primaryAction.copy(alpha = 0.65f),
                            uncheckedBorderColor = colors.background.copy(alpha = 0f),
                        ),
                    )
                }
            }

            HandHopHopButton(
                modifier = Modifier
                    .padding(top = dimensions.md)
                    .width(width),
                onClick = onClick,
                size = ButtonState.Size.FIX,
                textColor = ButtonState.Color.Button,
                buttonColor = ButtonState.Color.BottomBar,
            ) {
                Text(
                    text = "Очистить данные",
                    fontSize = fontSize,
                    color = colors.primaryAction,
                )
            }
        }
    }
}
