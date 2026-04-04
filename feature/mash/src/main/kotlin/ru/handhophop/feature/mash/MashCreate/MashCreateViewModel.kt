package ru.handhophop.feature.mash.MashCreate

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class MashCreateViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MashCreateUiState())
    internal val uiState: StateFlow<MashCreateUiState> = _uiState.asStateFlow()

    internal fun handleAction(action: MashCreateUiAction) {
        when (action) {
            is InitMashCreateAction -> {
                _uiState.update { state ->
                    state.copy(
                        imageUrl = action.imageUrl,
                        projectName = state.projectName.ifBlank { action.suggestedProjectName }
                    )
                }
            }

            is ProjectNameChangedAction -> {
                _uiState.update { state ->
                    state.copy(projectName = action.value)
                }
            }

            is ClearProjectNameAction -> {
                _uiState.update { state ->
                    state.copy(projectName = "")
                }
            }

            is SchemeTypeChangedAction -> {
                _uiState.update { state ->
                    state.copy(schemeType = action.value)
                }
            }

            is ColorCountChangedAction -> {
                _uiState.update { state ->
                    state.copy(
                        colorCount = action.value.coerceIn(
                            MASH_CREATE_MIN_COLORS,
                            MASH_CREATE_MAX_COLORS
                        )
                    )
                }
            }

            is DifficultyChangedAction -> {
                _uiState.update { state ->
                    state.copy(difficulty = action.value)
                }
            }

            is CreateWorkAction -> {
                val state = _uiState.value
                if (!state.isCreateButtonEnabled) return

                _uiState.update {
                    it.copy(
                        createdConfig = MashCreateConfig(
                            projectName = state.projectName.trim(),
                            imageUrl = state.imageUrl,
                            schemeType = state.schemeType,
                            colorCount = state.colorCount,
                            difficulty = state.difficulty,
                            threads = state.threads,
                        )
                    )
                }
            }

            is ConsumeCreatedConfigAction -> {
                _uiState.update { state ->
                    state.copy(createdConfig = null)
                }
            }
        }
    }
}