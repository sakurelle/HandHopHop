package ru.handhophop.feature.settings

internal data class SettingsUiState(
    val name: String,
    val nickname: String,
    val email: String,
    val phone: String,
    val avatarUrl: String? = null
)
