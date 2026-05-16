package ru.handhophop.feature.settings

import android.content.Context
import java.util.concurrent.TimeUnit
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.handhophop.core.network.voucher.VoucherNetworkService
import ru.handhophop.core.session.PremiumProvider
import ru.handhophop.core.system.database.work.WorkLocalRepository

class SettingViewModel(
    private val repository: WorkLocalRepository,
    private val context: Context
) : ViewModel() {



    private val _uiState = mutableStateOf(
        SettingUiState(
            isPremium = PremiumProvider.isPremium(),
            storageText = context.getString(R.string.zero_mb),
            premiumRemainingText = getRemainingPremiumText()
        )
    )
    val uiState: State<SettingUiState> = _uiState

    private val MAX_SIZE_MB = 512f

    init {
        updateStorageStats()
    }

    fun getVoucher():String{
        return PremiumProvider.getUserHash()
    }

    private fun getRemainingPremiumText(): String {
        val remainingMs = PremiumProvider.getRemainingTime()

        if (remainingMs <= 0) {
            return if (PremiumProvider.isPremium()) {
                context.getString(R.string.premium_expired)
            } else {
                context.getString(R.string.no_premium)
            }
        }

        val days = TimeUnit.MILLISECONDS.toDays(remainingMs)
        val hours = TimeUnit.MILLISECONDS.toHours(remainingMs) % 24

        return context.getString(R.string.premium_remaining_format, days, hours)
    }

    fun checkVoucher(userInput: String) {
        if (userInput.isBlank()) {
            _uiState.value = _uiState.value.copy(
                voucherError = context.getString(R.string.error_empty_voucher)
            )
            return
        }

        viewModelScope.launch {
            val currentValidHash = PremiumProvider.getUserHash()

            if (userInput == currentValidHash) {

                PremiumProvider.setPremiumStatus(true, userInput)


                _uiState.value = _uiState.value.copy(
                    isPremium = true,
                    voucherError = null,
                    premiumRemainingText = getRemainingPremiumText()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    voucherError = context.getString(R.string.error_invalid_voucher)
                )
            }
        }
    }

    fun updateStorageStats() {
        viewModelScope.launch {
            val bytes = repository.getDatabaseSize(context)
            val count = repository.getWorkCount()
            val megabytes = bytes / (1024 * 1024).toDouble()


            val text = when {
                count == 0 -> context.getString(R.string.zero_mb)
                megabytes < 0.1 -> context.getString(R.string.size_format_precise, megabytes)
                else -> context.getString(R.string.size_format_standard, megabytes)
            }

            val progress = (megabytes.toFloat() / MAX_SIZE_MB).coerceIn(0f, 1f)

            _uiState.value = _uiState.value.copy(
                storageText = text,
                storageProgress = if (count == 0) 0f else progress
            )
        }
    }

    fun clearDatabase() {
        viewModelScope.launch {
            repository.clearAllWorks()
            updateStorageStats()
        }
    }
}