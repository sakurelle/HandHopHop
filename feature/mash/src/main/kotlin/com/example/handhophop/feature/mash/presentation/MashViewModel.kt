package com.example.handhophop.feature.mash.presentation

internal class MashViewModel: ViewModel() {

    private val _uiState = MutableStateFlow<MashUiState>(MashUiState())
    val uiState: StateFlow<MashUiState> = _uiState

    fun handleAction(action: UiAction) {
        when (action) {
            action is ClickDownloadsAction -> {
                download()
            }
            action is GenerateShemaAction -> {
                //TODO делаем генерацию схемы
            }
        }
    }

    private fun download() {

    }

}