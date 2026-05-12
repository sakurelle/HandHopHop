package ru.handhophop.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
    viewModel: SettingViewModel,
    modifier: Modifier = Modifier,
) {
    var isChecked by remember { mutableStateOf(false) }
    var isCheckedTheme by remember { mutableStateOf(false) }
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val showDialog = remember { mutableStateOf(false) }

    val storageText by viewModel.storageText
    val storageProgress by viewModel.storageProgress

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.updateStorageStats()
    }


    val fontSize = dimensionResource(DesignR.dimen.font_size).value.sp
    val radius = dimensionResource(DesignR.dimen.radius)
    val border = dimensionResource(DesignR.dimen.border)
    val width = dimensionResource(DesignR.dimen.width)
    val heightBlock = dimensionResource(DesignR.dimen.height_block)

    val heightButton = dimensionResource(R.dimen.height_button)

    val text = stringResource(R.string.dark_theme)
    val clearData = stringResource(R.string.clear_data)
    val memory = stringResource(R.string.memory)
    val dialogTitle = stringResource(R.string.dialog_warning_title)
    val dialogMessage = stringResource(R.string.dialog_warning_message)
    val dialogConfirm = stringResource(R.string.dialog_confirm)
    val dialogDismiss = stringResource(R.string.dialog_dismiss)


    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            modifier = Modifier.border(
                width = border*2,
                color = colors.primaryAction,
                shape = RoundedCornerShape(radius)
            ),
            title = {
                Text(
                    modifier = Modifier,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    text = dialogTitle
                )
            },
            text = {
                Text(
                    modifier = Modifier,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                    text = dialogMessage
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearDatabase()
                        showDialog.value = false
                    }
                ) {
                    Text(
                        modifier = Modifier,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                        text=dialogConfirm,
                        color = Color.Red,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog.value = false }
                ) {
                    Text(
                        modifier = Modifier,
                        text=dialogDismiss,
                        color = colors.primaryAction
                    )
                }
            },
            containerColor = colors.topBar,
            shape = RoundedCornerShape(radius),

        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter

    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar(
                state= TopBarState(
                    R.string.setting,
                    null,
                    null
                ),
                { Unit},
                {Unit}
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
                        checked = isCheckedTheme,
                        onCheckedChange = { checked ->
                            isCheckedTheme = checked
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

            Box(
                modifier = Modifier
                    .padding(
                        top = dimensions.md
                    )
                    .width(width = width)
                    .wrapContentHeight()
                    .border(
                        border,
                        colors.primaryAction,
                        shape = RoundedCornerShape(radius)
                    )
                    .background(
                        color = colors.topBar,
                        shape = RoundedCornerShape(
                            radius
                        )
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(dimensions.md)
                ) {
                    Text(
                        text = memory,
                        fontSize = fontSize,
                    )

                    Spacer(modifier = Modifier.height(dimensions.md))

                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { storageProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(radius)),
                        color = colors.primaryAction, // Темная часть
                        trackColor = colors.white, // Светлая часть
                    )

                    Spacer(modifier = Modifier.height(dimensions.md / 2))

                    Text(
                        text = storageText,
                        fontSize = fontSize,
                    )
                }
            }
            HandHopHopButton(
                modifier = Modifier
                    .padding(top = dimensions.md)
                    .width(width),
                onClick = {
                    showDialog.value = true
                },
                size = ButtonState.Size.FIX,
                textColor = ButtonState.Color.Button,
                buttonColor = ButtonState.Color.BottomBar,
            ) {
                Text(
                    text = clearData,
                    fontSize = fontSize,
                    color = colors.primaryAction,
                )
            }
        }
    }

}
