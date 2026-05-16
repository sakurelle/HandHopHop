package ru.handhophop.feature.mash

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.handhophop.core.system.database.HandHopHopDatabaseProvider
import ru.handhophop.core.system.database.work.WorkLocalItem
import ru.handhophop.core.system.database.work.WorkLocalRepository
import ru.handhophop.core.system.database.work.hasStartedWork
import ru.handhophop.feature.mash.MashCreate.MashCreateScreen
import ru.handhophop.feature.mash.Statistics.MashStatisticsScreen
import java.util.Locale

private const val DEFAULT_MASH_IMAGE_URL =
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQDqAZfJ7DSp_ML801Txp-yEJ5zTXIDtbM9AQ&s"

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
    onBottomBarVisibilityChanged: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val pdfSavedMessageTemplate = stringResource(R.string.mash_pdf_saved)
    val pdfFailedMessage = stringResource(R.string.mash_pdf_failed)

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
    var pendingDownloadTitle by remember { mutableStateOf<String?>(null) }

    var destination by rememberSaveable {
        mutableStateOf(
            if (initialWorkId != null || initialImageUrl != null) {
                MashDestination.CREATE
            } else {
                MashDestination.HOME
            },
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { _ ->
        val projectTitle = pendingDownloadTitle
        pendingDownloadTitle = null

        projectTitle?.let {
            viewModel.handleAction(
                ClickDownloadsAction(
                    projectTitle = it,
                ),
            )
        }
    }

    LaunchedEffect(viewModel, context, pdfSavedMessageTemplate, pdfFailedMessage) {
        viewModel.events.collect { event ->
            when (event) {
                is MashEvent.ExportPdf -> {
                    val result = withContext(Dispatchers.IO) {
                        exportSchemePdf(
                            context = context,
                            projectTitle = event.projectTitle,
                            scheme = event.scheme,
                        )
                    }

                    result.onSuccess { savedPdfFile ->
                        Toast.makeText(
                            context,
                            String.format(
                                Locale.getDefault(),
                                pdfSavedMessageTemplate,
                                savedPdfFile.fileName,
                            ),
                            Toast.LENGTH_LONG,
                        ).show()

                        openSavedPdf(context, savedPdfFile)
                        showSavedPdfNotificationIfAllowed(context, savedPdfFile)
                    }.onFailure {
                        Toast.makeText(
                            context,
                            pdfFailedMessage,
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }
            }
        }
    }

    LaunchedEffect(createdConfig, workImageBytes) {
        val config = createdConfig ?: return@LaunchedEffect
        val imageBytes = workImageBytes ?: return@LaunchedEffect

        if (config != lastGeneratedConfig) {
            lastGeneratedConfig = config
            viewModel.handleAction(
                GenerateSchemeAction(
                    config = config,
                    imageBytes = imageBytes,
                ),
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
        val config = createdConfig ?: return@LaunchedEffect

        if (workImageBytes != null) {
            return@LaunchedEffect
        }

        val imageUrl = config.imageUrl
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_MASH_IMAGE_URL

        val bitmap = loadBitmapFromUrl(
            context = context,
            url = imageUrl,
        ) ?: return@LaunchedEffect

        workImageBytes = bitmapToByteArray(bitmap)
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

    LaunchedEffect(createdConfig) {
        val config = createdConfig ?: return@LaunchedEffect
        val url = config.imageUrl?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect

        currentWorkId = repository.addFavorite(
            WorkLocalItem(
                id = currentWorkId ?: 0,
                url = url,
                image = workImageBytes,
                isFavorite = true,
            ),
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
            ),
        )
    }

    LaunchedEffect(uiState.scheme, pendingCompletedCells) {
        val restoredCompletedCells = pendingCompletedCells ?: return@LaunchedEffect

        if (uiState.scheme == null) {
            return@LaunchedEffect
        }

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
            )
        }

        MashDestination.CREATE -> {
            MashCreateScreen(
                imageUrl = initialImageUrl ?: createdConfig?.imageUrl,
                onBackClick = { destination = MashDestination.HOME },
                onCreateFinished = { newConfig ->
                    if (createdConfig?.imageUrl != newConfig.imageUrl) {
                        workImageBytes = null
                    }

                    lastGeneratedConfig = null
                    createdConfig = newConfig
                    destination = MashDestination.WORKSPACE
                },
            )
        }

        MashDestination.WORKSPACE -> {
            MashScreen(
                title = createdConfig?.projectName.orEmpty(),
                uiState = uiState,
                onBackClick = { destination = MashDestination.HOME },
                onDownloadClick = {
                    val projectTitle = createdConfig?.projectName.orEmpty()

                    if (needsNotificationPermission(context)) {
                        pendingDownloadTitle = projectTitle
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.handleAction(
                            ClickDownloadsAction(
                                projectTitle = projectTitle,
                            ),
                        )
                    }
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

private fun needsNotificationPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
private fun showSavedPdfNotificationIfAllowed(
    context: Context,
    savedPdfFile: SavedPdfFile,
) {
    val hasNotificationPermission =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED

    if (hasNotificationPermission) {
        showSavedPdfNotification(context, savedPdfFile)
    }
}