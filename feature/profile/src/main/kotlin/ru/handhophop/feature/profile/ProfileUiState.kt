package ru.handhophop.feature.profile

internal data class ProfileUiState(
    val name: String,
    val nickname: String,
    val email: String,
    val phone: String,
    val avatarUrl: String? = null
)