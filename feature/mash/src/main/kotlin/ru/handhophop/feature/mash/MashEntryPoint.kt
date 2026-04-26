package ru.handhophop.feature.mash

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.handhophop.feature.mash.MashCreate.MashCreateConfig
import ru.handhophop.feature.mash.MashCreate.MashCreateScreen
import ru.handhophop.feature.mash.Statistics.MashStatisticsScreen

private enum class MashDestination {
    CREATE,
    WORKSPACE,
    STATISTICS,
}

@Composable
fun MashEntryPoint(
    initialImageUrl: String? = null,
) {
    val viewModel: MashViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var createdConfig by remember { mutableStateOf<MashCreateConfig?>(null) }
    var destination by rememberSaveable { mutableStateOf(MashDestination.STATISTICS) }

    LaunchedEffect(createdConfig) {
        createdConfig?.let { config ->
            viewModel.handleAction(GenerateSchemeAction(config))
        }
    }

    BackHandler(enabled = destination != MashDestination.STATISTICS) {
        destination = MashDestination.STATISTICS
    }

    when (destination) {
        MashDestination.CREATE -> {
            MashCreateScreen(
                imageUrl = initialImageUrl,
                onCreateFinished = { newConfig ->
                    createdConfig = newConfig
                    destination = MashDestination.WORKSPACE
                }
            )
        }

        MashDestination.WORKSPACE -> {
            MashScreen(
                uiState = uiState,
                onDownloadClick = {
                    viewModel.handleAction(ClickDownloadsAction())
                },
                onSchemeCellClick = { cellIndex ->
                    viewModel.handleAction(ClickSchemeCellAction(cellIndex))
                },
                onHighlightColorToggle = { paletteIndex ->
                    viewModel.handleAction(TogglePaletteHighlightAction(paletteIndex))
                },
                onPaletteCompletionToggle = { paletteIndex ->
                    viewModel.handleAction(TogglePaletteCompletedAction(paletteIndex))
                },
                onClearSelection = {
                    viewModel.handleAction(ClearPaletteHighlightAction())
                },
            )
        }

        MashDestination.STATISTICS -> {
            MashStatisticsScreen(
                projectConfig = createdConfig,
                uiState = uiState,
                onCreateProjectClick = { destination = MashDestination.CREATE },
                onOpenProjectClick = { destination = MashDestination.WORKSPACE },
            )
        }
    }
}
