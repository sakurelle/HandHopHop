package ru.handhophop.feature.settings

import android.widget.ToggleButton
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.handhophop.core.design.TopBar
import ru.handhophop.core.design.TopBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onChangeTheme: (isActive: Boolean) -> Unit,
    viewModel: SettingViewModel,
    modifier: Modifier = Modifier,
) {
    val isChecked = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }

    val storageText by viewModel.storageText
    val storageProgress by viewModel.storageProgress

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.updateStorageStats()
    }


    val checkedThumbColor = colorResource(R.color.profile_background)
    val checkedTrackColor = colorResource(R.color.button)
    val mainColor = colorResource(R.color.main_color)
    val buttonColor = colorResource(R.color.bottom_bar)

    val fontSize = dimensionResource(R.dimen.font_size).value.sp
    val radius = dimensionResource(R.dimen.radius)
    val border = dimensionResource(R.dimen.border)
    val padding = dimensionResource(R.dimen.padding)
    val width = dimensionResource(R.dimen.width)
    val heightBlock = dimensionResource(R.dimen.height_block)
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
                color = checkedTrackColor,
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
                        color = checkedTrackColor
                    )
                }
            },
            containerColor = mainColor,
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
                state = TopBarState(
                    R.string.setting,
                    null,
                    null
                ),
                { Unit },
                { Unit }
            )
            Box(
                modifier = Modifier
                    .padding(
                        top = padding
                    )
                    .width(width = width)
                    .height(heightBlock)
                    .border(
                        border,
                        checkedTrackColor,
                        shape = RoundedCornerShape(radius)
                    )
                    .background(
                        color = mainColor,
                        shape = RoundedCornerShape(
                            radius
                        )
                    ),

                ) {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(
                            padding
                        )
                ) {
                    Text(
                        modifier = Modifier
                            .padding(
                                bottom = padding
                            ),
                        text = text,
                        fontSize = fontSize
                    )
                    Switch(
                        modifier = Modifier,
                        checked = isChecked.value,
                        onCheckedChange = {
                            !isChecked.value
                            onChangeTheme(isChecked.value)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = checkedThumbColor,
                            checkedTrackColor = checkedTrackColor,
                            uncheckedThumbColor = checkedThumbColor,
                            uncheckedTrackColor = checkedTrackColor.copy(0.65f),
                            uncheckedBorderColor = checkedThumbColor.copy(0f)
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(
                        top = padding
                    )
                    .width(width = width)
                    .wrapContentHeight()
                    .border(
                        border,
                        checkedTrackColor,
                        shape = RoundedCornerShape(radius)
                    )
                    .background(
                        color = mainColor,
                        shape = RoundedCornerShape(
                            radius
                        )
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(padding)
                ) {
                    Text(
                        text = memory,
                        fontSize = fontSize,
                    )

                    Spacer(modifier = Modifier.height(padding))

                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { storageProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(radius)),
                        color = checkedTrackColor, // Темная часть
                        trackColor = Color.White, // Светлая часть
                    )

                    Spacer(modifier = Modifier.height(padding / 2))

                    Text(
                        text = storageText,
                        fontSize = fontSize,
                    )
                }
            }

            Button(
                modifier = Modifier
                    .padding(
                        top = padding
                    )
                    .border(
                        border,
                        checkedTrackColor,
                        shape = RoundedCornerShape(radius)
                    )
                    .height(heightButton)

                    .width(width = width),
                onClick = {
                    showDialog.value = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor
                ),
                shape = RoundedCornerShape(radius)
            ) {
                Text(
                    text = clearData,
                    fontSize = fontSize,
                    color = checkedTrackColor
                )
            }
        }
    }

}
