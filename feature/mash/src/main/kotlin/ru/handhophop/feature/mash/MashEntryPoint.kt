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
    HOME,
    CREATE,
    WORKSPACE,
    STATISTICS,
}

@Composable
fun MashEntryPoint(
    initialImageUrl: String? = null,
    onBottomBarVisibilityChanged: (Boolean) -> Unit = {},
) {
    val viewModel: MashViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val cachedWork = remember { MashWorkCache.currentWork }
    var createdConfig by remember { mutableStateOf(cachedWork?.config) }
    var lastGeneratedConfig by remember { mutableStateOf(cachedWork?.config) }
    var destination by rememberSaveable {
        mutableStateOf(
            if (initialImageUrl != null) {
                MashDestination.CREATE
            } else {
                MashDestination.HOME
            }
        )
    }

    LaunchedEffect(createdConfig) {
        val config = createdConfig ?: return@LaunchedEffect
        if (config != lastGeneratedConfig) {
            lastGeneratedConfig = config
            viewModel.handleAction(GenerateSchemeAction(config))
        }
    }

    LaunchedEffect(cachedWork) {
        cachedWork?.let { work ->
            viewModel.restoreCachedWork(work.uiState)
        }
    }

    LaunchedEffect(initialImageUrl) {
        if (initialImageUrl != null) {
            destination = MashDestination.CREATE
        }
    }

    LaunchedEffect(destination) {
        onBottomBarVisibilityChanged(destination != MashDestination.CREATE)
    }

    LaunchedEffect(createdConfig, uiState) {
        MashWorkCache.currentWork = createdConfig?.let { config ->
            CachedMashWork(
                config = config,
                uiState = uiState,
            )
        }
    }

    BackHandler(enabled = destination != MashDestination.HOME) {
        destination = MashDestination.HOME
    }

    when (destination) {
        MashDestination.HOME -> {
            MashHomeScreen(
                projectConfig = createdConfig,
                uiState = uiState,
                onCreateProjectClick = { destination = MashDestination.CREATE },
                onOpenProjectClick = { destination = MashDestination.WORKSPACE },
                onOpenStatisticsClick = { destination = MashDestination.STATISTICS },
            )
        }

        MashDestination.CREATE -> {
            MashCreateScreen(
                imageUrl = initialImageUrl ?: createdConfig?.imageUrl,
                onBackClick = { destination = MashDestination.HOME },
                onCreateFinished = { newConfig ->
                    createdConfig = newConfig
                    destination = MashDestination.WORKSPACE
                }
            )
        }

        MashDestination.WORKSPACE -> {
            MashScreen(
                title = createdConfig?.projectName ?: "",
                uiState = uiState,
                onBackClick = { destination = MashDestination.HOME },
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
                onBackClick = { destination = MashDestination.HOME },
                onCreateProjectClick = { destination = MashDestination.CREATE },
                onOpenProjectClick = { destination = MashDestination.WORKSPACE },
            )
        }
    }
}
