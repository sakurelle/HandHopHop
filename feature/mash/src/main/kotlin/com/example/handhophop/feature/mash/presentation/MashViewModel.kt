package com.example.handhophop.feature.mash.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class MashViewModel: ViewModel() {

    private val _uiState = MutableStateFlow<MashUiState>(MashUiState())
    val uiState: StateFlow<MashUiState> = _uiState

    fun handleAction(action: UiAction) {
        when (action) {
            is ClickDownloadsAction -> {
                // TODO делаем загрузку схемы
            }
            is GenerateShemaAction -> {
                //TODO делаем генерацию схемы
            }
            is HighlightingColorAction -> {
                //TODO делаем выделение определенного цвета
            }
            is ShadedColorAction -> {
                //TODO делаем вывод итогового результата по двойному нажатию
            }
        }
    }

    private fun download() {

    }

}