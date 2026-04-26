package ru.handhophop.feature.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val SETTINGS_NAME = "Егор Иванов"
private const val SETTINGS_NICKNAME = "@abober4000"
private const val SETTINGS_EMAIL = "egor@example.com"
private const val SETTINGS_PHONE = "+7 (999) 123-45-67"
private const val SETTINGS_AVATAR_URL = "https://i.pravatar.cc/400?img=12"

internal class SettingsViewModel {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            name = SETTINGS_NAME,
            nickname = SETTINGS_NICKNAME,
            email = SETTINGS_EMAIL,
            phone = SETTINGS_PHONE,
            avatarUrl = SETTINGS_AVATAR_URL
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
}
