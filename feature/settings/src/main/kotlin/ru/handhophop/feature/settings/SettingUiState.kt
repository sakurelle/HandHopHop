package ru.handhophop.feature.settings

data class SettingUiState(
    val isPremium: Boolean = false,
    val voucherError: String? = null,
    val storageText: String = "0 mb",
    val storageProgress: Float = 0f,
    val premiumRemainingText: String = ""
) {
}