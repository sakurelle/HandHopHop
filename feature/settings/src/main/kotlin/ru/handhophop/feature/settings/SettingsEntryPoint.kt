package ru.handhophop.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun SettingsEntryPoint() {
    val viewModel = remember { SettingsViewModel() }
    SettingsScreen(viewModel = viewModel)
}
