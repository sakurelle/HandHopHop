package ru.handhophop.feature.profile

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PROFILE_NAME = "Егор Иванов"
private const val PROFILE_NICKNAME = "@abober4000"
private const val PROFILE_EMAIL = "egor@example.com"
private const val PROFILE_PHONE = "+7 (999) 123-45-67"
private const val PROFILE_AVATAR_URL = "https://i.pravatar.cc/400?img=12"

internal class ProfileViewModel {

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            name = PROFILE_NAME,
            nickname = PROFILE_NICKNAME,
            email = PROFILE_EMAIL,
            phone = PROFILE_PHONE,
            avatarUrl = PROFILE_AVATAR_URL
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
}