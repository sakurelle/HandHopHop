package ru.handhophop.feature.mash

import androidx.activity.compose.BackHandler
import androidx.activity.result.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ru.handhophop.core.system.database.HandHopHopDatabaseProvider
import ru.handhophop.core.system.database.work.WorkLocalItem
import ru.handhophop.core.system.database.work.WorkLocalRepository
import ru.handhophop.core.system.database.work.hasStartedWork
import ru.handhophop.feature.feed.presentation.FeedEntryPoint
import ru.handhophop.feature.mash.MashCreate.FeedPopupScreen
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
    initialWorkId: Long? = null,
    initialImageUrl: String? = null,
    backgroundContent: @Composable () -> Unit,
    onBack: () -> Unit,
    onBottomBarVisibilityChanged: (Boolean) -> Unit = {},
) {
    var showPopup by rememberSaveable { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val repository = remember(context) {
        WorkLocalRepository(
            workDao = HandHopHopDatabaseProvider.get(context).workDao(),
        )
    }
    val viewModel: MashViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val cachedWork = remember(initialWorkId, initialImageUrl) {
        if (initialWorkId == null && initialImageUrl == null) {
            MashWorkCache.currentWork
        } else {
            null
        }
    }
    var workImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var currentWorkId by remember { mutableStateOf<Long?>(null) }
    var createdConfig by remember { mutableStateOf(cachedWork?.config) }
    var lastGeneratedConfig by remember { mutableStateOf(cachedWork?.config) }
    var pendingCompletedCells by remember { mutableStateOf<Set<Int>?>(null) }
    var destination by rememberSaveable {
        mutableStateOf(
            if (initialWorkId != null || initialImageUrl != null) {
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
            viewModel.handleAction(
                GenerateSchemeAction(
                    config = config,
                    imageBytes = workImageBytes,
                )
            )
        }
    }

    LaunchedEffect(cachedWork) {
        cachedWork?.let { work ->
            viewModel.restoreCachedWork(work.uiState)
        }
    }

    LaunchedEffect(initialWorkId, initialImageUrl) {
        if (initialWorkId != null || initialImageUrl != null) {
            val persistedWork = when {
                initialWorkId != null -> repository.getWorkById(initialWorkId)
                initialImageUrl != null -> repository.getWorkByUrl(initialImageUrl)
                else -> null
            }
            val persistedConfig = persistedWork?.toMashCreateConfigOrNull()
            if (persistedWork != null && persistedConfig != null && persistedWork.hasStartedWork()) {
                currentWorkId = persistedWork.id
                workImageBytes = persistedWork.image
                createdConfig = persistedConfig
                pendingCompletedCells = persistedWork.decodeCompletedCells()
                destination = MashDestination.WORKSPACE
            } else {
                if (persistedWork != null && persistedWork.hasStartedWork()) {
                    repository.removeWork(persistedWork.id)
                }
                createdConfig = null
                currentWorkId = persistedWork?.id
                workImageBytes = persistedWork?.image
                pendingCompletedCells = null
                destination = MashDestination.CREATE
            }
        } else if (createdConfig == null) {
            val persistedWork = repository.getAllWorks()
                .firstOrNull { it.hasStartedWork() }
            val persistedConfig = persistedWork?.toMashCreateConfigOrNull()
            if (persistedWork != null && persistedConfig != null) {
                currentWorkId = persistedWork.id
                workImageBytes = persistedWork.image
                createdConfig = persistedConfig
                pendingCompletedCells = persistedWork.decodeCompletedCells()
                destination = MashDestination.HOME
            } else if (persistedWork != null && persistedWork.hasStartedWork()) {
                repository.removeWork(persistedWork.id)
            }
        }
    }

    LaunchedEffect(createdConfig?.imageUrl, workImageBytes) {
        val imageUrl = createdConfig?.imageUrl?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        if (workImageBytes != null) return@LaunchedEffect

        val bitmap = loadBitmapFromUrl(
            context = context,
            url = imageUrl,
        ) ?: return@LaunchedEffect

        workImageBytes = bitmapToByteArray(bitmap)
    }

    LaunchedEffect(destination, showPopup) {
        onBottomBarVisibilityChanged((destination != MashDestination.CREATE)|| showPopup )
    }

    LaunchedEffect(createdConfig, uiState) {
        MashWorkCache.currentWork = createdConfig?.let { config ->
            CachedMashWork(
                config = config,
                uiState = uiState,
            )
        }
    }

    LaunchedEffect(createdConfig) {
        val config = createdConfig ?: return@LaunchedEffect
        val url = config.imageUrl?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect

        currentWorkId = repository.addFavorite(
            WorkLocalItem(
                id = currentWorkId ?: 0,
                url = url,
                image = workImageBytes,
                isFavorite = true,
            )
        )
    }

    LaunchedEffect(createdConfig, uiState.scheme, uiState.completedCellIndices) {
        val config = createdConfig ?: return@LaunchedEffect
        val url = config.imageUrl?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect

        currentWorkId = repository.addWork(
            config.toWorkLocalItem(
                id = currentWorkId ?: 0,
                image = workImageBytes,
                isFavorite = repository.getWorkByUrl(url)?.isFavorite == true,
                uiState = uiState,
            )
        )
    }

    LaunchedEffect(uiState.scheme, pendingCompletedCells) {
        val restoredCompletedCells = pendingCompletedCells ?: return@LaunchedEffect
        if (uiState.scheme == null) return@LaunchedEffect

        viewModel.restoreCompletedCells(restoredCompletedCells)
        pendingCompletedCells = null
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
                onDeleteSheme = {
                    scope.launch {
                        currentWorkId?.let { id ->
                            repository.removeWork(id)

                            currentWorkId = null
                            createdConfig = null
                            workImageBytes = null
                            pendingCompletedCells = null


                            viewModel.resetWork()

                            MashWorkCache.currentWork = null
                        }
                    }
                }
            )
        }

        MashDestination.CREATE -> {

            if (showPopup) {
                backgroundContent()
                FeedPopupScreen(
                    imageUrl = initialImageUrl ?: createdConfig?.imageUrl ?: "",
                    onStartWork = {
                        showPopup = false
                    },
                    onClose = {
                        onBack()
                    },
                    onSave = {

                    }
                )
            } else {
                MashCreateScreen(
                    imageUrl = initialImageUrl ?: createdConfig?.imageUrl,
                    onBackClick = {
                        onBack()
                    },
                    onCreateFinished = { newConfig ->
                        createdConfig = newConfig
                        destination = MashDestination.WORKSPACE
                    }
                )
            }
            /*MashCreateScreen(
                imageUrl = initialImageUrl ?: createdConfig?.imageUrl,
                onBackClick = { destination = MashDestination.HOME },
                onCreateFinished = { newConfig ->
                    createdConfig = newConfig
                    destination = MashDestination.WORKSPACE
                }
            )*/
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
