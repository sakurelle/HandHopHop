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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.handhophop.core.design.ButtonState
import ru.handhophop.core.design.HandHopHopButton
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.core.design.ThemeMode
import ru.handhophop.core.design.TopBar
import ru.handhophop.core.design.TopBarState
import ru.handhophop.core.session.PremiumProvider
import ru.handhophop.design.R as DesignR

@Composable
fun SettingScreen(
    currentThemeMode: ThemeMode,
    isDarkTheme: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    viewModel: SettingViewModel,
    modifier: Modifier = Modifier,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val showDialog = remember { mutableStateOf(false) }


    val uiState by viewModel.uiState

    val premiumRemainingText = uiState.premiumRemainingText
    LaunchedEffect(Unit) {
        viewModel.updateStorageStats()
    }

    val fontSize = dimensionResource(DesignR.dimen.font_size).value.sp
    val radius = dimensionResource(DesignR.dimen.radius)
    val border = dimensionResource(DesignR.dimen.border)
    val width = dimensionResource(DesignR.dimen.width)
    val heightBlock = dimensionResource(DesignR.dimen.height_block)

    val clearData = stringResource(R.string.clear_data)
    val memory = stringResource(R.string.memory)
    val dialogTitle = stringResource(R.string.dialog_warning_title)
    val dialogMessage = stringResource(R.string.dialog_warning_message)
    val dialogConfirm = stringResource(R.string.dialog_confirm)
    val dialogDismiss = stringResource(R.string.dialog_dismiss)
    val systemThemeButtonText = stringResource(R.string.system_theme_button)
    val currentThemeLabel = stringResource(R.string.current_theme_label)

    val currentThemeValue = when (currentThemeMode) {
        ThemeMode.SYSTEM -> stringResource(
            if (isDarkTheme) {
                R.string.theme_mode_system_dark
            } else {
                R.string.theme_mode_system_light
            },
        )

        ThemeMode.DARK -> stringResource(R.string.theme_mode_dark)
        ThemeMode.LIGHT -> stringResource(R.string.theme_mode_light)
    }

    val themeDescription = if (currentThemeMode == ThemeMode.SYSTEM) {
        stringResource(R.string.theme_mode_follow_system_description)
    } else {
        stringResource(R.string.theme_mode_manual_description)
    }

    val showPremiumDialog = remember { mutableStateOf(false) }
    val voucherText = remember { mutableStateOf("") }
    val isPremium = uiState.isPremium
    val storageText = uiState.storageText
    val storageProgress = uiState.storageProgress

    val premiumTitle = stringResource(R.string.premium_voucher_title)
    val premiumPlaceholder = stringResource(R.string.premium_voucher_placeholder)
    val premiumConfirm = stringResource(R.string.premium_voucher_confirm)
    val getPremiumText = stringResource(R.string.premium_get_button)
    val premiumActiveText = stringResource(R.string.premium_is_active)
    val voucherError = uiState.voucherError


    if (showPremiumDialog.value) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog.value = false },
            modifier = Modifier.border(
                width = border * 2,
                color = colors.primaryAction,
                shape = RoundedCornerShape(radius),
            ),
            title = {
                Text(
                    text = premiumTitle,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                )
            },
            text = {
                Column {
                    androidx.compose.material3.OutlinedTextField(
                        value = voucherText.value,
                        onValueChange = { voucherText.value = it },
                        placeholder = { Text(premiumPlaceholder) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = voucherError != null,
                        supportingText = {
                            if (voucherError != null) {
                                Text(text = voucherError!!, color = colors.error)
                            }
                        },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryAction,
                            unfocusedBorderColor = colors.textSecondary
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.checkVoucher(voucherText.value)
                        if (voucherError == null) showPremiumDialog.value = false
                    },
                ) {
                    Text(
                        text = premiumConfirm,
                        fontFamily = FontFamily.Default,
                        color = colors.primaryAction,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPremiumDialog.value = false }) {
                    Text(text = dialogDismiss, color = colors.textSecondary)
                }
            },
            containerColor = colors.topBar,
            shape = RoundedCornerShape(radius),
        )
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            modifier = Modifier.border(
                width = border * 2,
                color = colors.primaryAction,
                shape = RoundedCornerShape(radius),
            ),
            title = {
                Text(
                    text = dialogTitle,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                )
            },
            text = {
                Text(
                    text = dialogMessage,
                    fontFamily = FontFamily.Default,
                    color = colors.textSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearDatabase()
                        showDialog.value = false
                    },
                ) {
                    Text(
                        text = dialogConfirm,
                        fontFamily = FontFamily.Default,
                        color = colors.error,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog.value = false },
                ) {
                    Text(
                        text = dialogDismiss,
                        color = colors.primaryAction,
                    )
                }
            },
            containerColor = colors.topBar,
            shape = RoundedCornerShape(radius),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar(
                state = TopBarState(
                    titleRes = R.string.setting,
                    leftIconRes = null,
                    rightIconRes = null,
                ),
                onClickRight = {},
                onClickLeft = {},
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = currentThemeLabel,
                                fontSize = fontSize * 0.75f,
                                color = colors.textSecondary,
                            )

                            Text(
                                text = currentThemeValue,
                                fontSize = fontSize * 0.85f,
                                color = colors.textPrimary,
                            )
                        }

                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { checked ->
                                onThemeModeChange(
                                    if (checked) {
                                        ThemeMode.DARK
                                    } else {
                                        ThemeMode.LIGHT
                                    },
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.onPrimaryAction,
                                checkedTrackColor = colors.primaryAction,
                                uncheckedThumbColor = colors.onPrimaryAction,
                                uncheckedTrackColor = colors.primaryAction.copy(alpha = 0.65f),
                                uncheckedBorderColor = colors.onPrimaryAction.copy(alpha = 0f),
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensions.sm))

                    Text(
                        text = themeDescription,
                        fontSize = fontSize * 0.72f,
                        color = colors.textSecondary,
                    )

                    Spacer(modifier = Modifier.height(dimensions.sm))

                    HandHopHopButton(
                        onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                        size = ButtonState.Size.WRAPCONTENT,
                        textColor = ButtonState.Color.Button,
                        buttonColor = ButtonState.Color.Background,
                        modifier = Modifier.widthIn(min = width / 2),
                    ) {
                        Text(
                            text = systemThemeButtonText,
                            fontSize = fontSize * 0.72f,
                            color = colors.primaryAction,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(top = dimensions.md)
                    .width(width = width)
                    .wrapContentHeight()
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
                        text = memory,
                        fontSize = fontSize,
                        color = colors.textPrimary,
                    )

                    Spacer(modifier = Modifier.height(dimensions.md))

                    LinearProgressIndicator(
                        progress = { storageProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        color = colors.primaryAction,
                        trackColor = colors.surfaceSoft,
                    )

                    Spacer(modifier = Modifier.height(dimensions.md / 2))

                    Text(
                        text = storageText,
                        fontSize = fontSize,
                        color = colors.textSecondary,
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
            HandHopHopButton(
                modifier = Modifier
                    .padding(top = dimensions.md)
                    .width(width),
                onClick = {
                    if (!isPremium) {
                        showPremiumDialog.value = true
                    }
                },
                size = ButtonState.Size.FIX,
                textColor = ButtonState.Color.Button,
                buttonColor = ButtonState.Color.BottomBar,
            ) {
                Text(
                    text = if (isPremium) premiumActiveText else getPremiumText,
                    fontSize = fontSize,
                    color = if (isPremium) colors.textSecondary else colors.primaryAction,
                )
            }
            Spacer(modifier = Modifier.height(dimensions.xs))
            Text(
                text = PremiumProvider.getUserHash(),
                fontSize = fontSize,
                color = if (isPremium) colors.textSecondary else colors.primaryAction,
            )
            if (isPremium && premiumRemainingText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(dimensions.xs))
                Text(
                    text = premiumRemainingText,
                    fontSize = fontSize * 0.7f,
                    color = colors.textSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
