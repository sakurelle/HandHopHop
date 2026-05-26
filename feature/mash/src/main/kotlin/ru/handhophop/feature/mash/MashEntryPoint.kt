package ru.handhophop.feature.mash

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.handhophop.core.system.database.HandHopHopDatabaseProvider
import ru.handhophop.core.system.database.work.WorkActivityStats
import ru.handhophop.core.system.database.work.WorkLocalItem
import ru.handhophop.core.system.database.work.WorkLocalRepository
import ru.handhophop.core.system.database.work.hasCreatedWorkConfig
import ru.handhophop.feature.mash.MashCreate.FeedPopupScreen
import ru.handhophop.feature.mash.MashCreate.MashCreateScreen
import ru.handhophop.feature.mash.Statistics.MashStatisticsScreen
import ru.handhophop.feature.mash.completed.WorkCompletedEntryPoint
import java.util.Locale

private const val SPENT_TIME_SAVE_INTERVAL_MILLIS = 30_000L

private enum class MashDestination {
    HOME,
    CREATE,
    WORKSPACE,
    COMPLETED,
    STATISTICS,
}

@Composable
fun MashEntryPoint(
    initialWorkId: Long? = null,
    initialImageUrl: String? = null,
    onOpenFeed: () -> Unit,
    backgroundContent: @Composable () -> Unit,
    onBack: () -> Unit,
    onBottomBarVisibilityChanged: (Boolean) -> Unit = {},
) {
    var showPopup by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val pdfSavedMessageTemplate = stringResource(R.string.mash_pdf_saved)
    val pdfFailedMessage = stringResource(R.string.mash_pdf_failed)
    val imageLoadFailedMessage = stringResource(R.string.mash_create_image_load_failed)

    val repository = remember(context) {
        WorkLocalRepository(
            workDao = HandHopHopDatabaseProvider.get(context).workDao(),
            appContext = context.applicationContext,
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
    var currentWorkImagePath by remember { mutableStateOf<String?>(null) }
    var currentWorkId by remember { mutableStateOf<Long?>(null) }
    var createdConfig by remember { mutableStateOf(cachedWork?.config) }
    var lastGeneratedConfig by remember { mutableStateOf(cachedWork?.config) }
    var imageLoadAttemptKey by remember { mutableStateOf<String?>(null) }
    var createImageLoadFailed by remember { mutableStateOf(false) }
    var selectedCreateImageUrl by rememberSaveable(initialImageUrl) {
        mutableStateOf(initialImageUrl)
    }

    var forceOpenCompletedWorkspace by rememberSaveable {
        mutableStateOf(false)
    }
    var isRestoringPersistedProgress by rememberSaveable {
        mutableStateOf(false)
    }

    var pendingCompletedCells by remember {
        mutableStateOf<Set<Int>?>(null)
    }

    var pendingDownloadTitle by remember { mutableStateOf<String?>(null) }
    var spentTimeBaseMillis by rememberSaveable { mutableLongStateOf(0L) }
    var workspaceStartedAtMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var lastActivitySavedAtMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingActivityEndMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var spentTimeSaveTick by remember { mutableIntStateOf(0) }
    var activityStats by remember { mutableStateOf(WorkActivityStats()) }

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

    val localImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            val savedLocalImage = withContext(Dispatchers.IO) {
                copyPickedImageToAppStorage(
                    context = context,
                    uri = uri,
                )
            }

            if (savedLocalImage == null) {
                createImageLoadFailed = true
                Toast.makeText(
                    context,
                    imageLoadFailedMessage,
                    Toast.LENGTH_LONG,
                ).show()
                return@launch
            }

            selectedCreateImageUrl = null
            currentWorkImagePath = savedLocalImage.path
            workImageBytes = savedLocalImage.bytes
            imageLoadAttemptKey = buildImageLoadKey(
                imagePath = savedLocalImage.path,
                imageUrl = null,
            )
            createImageLoadFailed = false
            lastGeneratedConfig = null
            createdConfig = createdConfig?.copy(
                imageUrl = null,
                imagePath = savedLocalImage.path,
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

    LaunchedEffect(createdConfig, currentWorkImagePath, workImageBytes, imageLoadAttemptKey, pendingCompletedCells) {
        val config = createdConfig ?: return@LaunchedEffect
        val loadKey = buildImageLoadKey(
            imagePath = currentWorkImagePath ?: config.imagePath,
            imageUrl = config.imageUrl,
        )

        if (workImageBytes == null && imageLoadAttemptKey != loadKey) {
            return@LaunchedEffect
        }

        if (config != lastGeneratedConfig) {
            val completedCellsForRestore = pendingCompletedCells.orEmpty()

            lastGeneratedConfig = config
            pendingCompletedCells = null

            viewModel.handleAction(
                GenerateSchemeAction(
                    config = config,
                    imageBytes = workImageBytes,
                    initialCompletedCellIndices = completedCellsForRestore,
                ),
            )

            isRestoringPersistedProgress = false
        }
    }

    LaunchedEffect(cachedWork) {
        cachedWork?.let { work ->
            viewModel.restoreCachedWork(work.uiState)
        }
    }

    LaunchedEffect(initialWorkId, initialImageUrl) {
        if (initialWorkId != null || initialImageUrl != null) {
            if (!initialImageUrl.isNullOrBlank()) {
                selectedCreateImageUrl = initialImageUrl
                currentWorkImagePath = null
                workImageBytes = null
                createImageLoadFailed = false
            }

            val persistedWork = repository.getWorkForOpening(
                id = initialWorkId,
                url = initialImageUrl,
            )

            val persistedConfig = persistedWork?.toMashCreateConfigOrNull()

            if (persistedWork != null && persistedConfig != null) {
                currentWorkId = persistedWork.id
                workImageBytes = null
                currentWorkImagePath = persistedWork.imagePath
                imageLoadAttemptKey = null
                createdConfig = persistedConfig

                pendingCompletedCells = persistedWork.decodeCompletedCells()
                isRestoringPersistedProgress = true

                spentTimeBaseMillis = persistedWork.spentTime ?: 0L
                workspaceStartedAtMillis = null
                lastActivitySavedAtMillis = null
                pendingActivityEndMillis = null
                destination = MashDestination.WORKSPACE
            } else {
                createdConfig = null
                currentWorkId = persistedWork?.id
                workImageBytes = null
                currentWorkImagePath = persistedWork?.imagePath
                imageLoadAttemptKey = null

                pendingCompletedCells = null
                isRestoringPersistedProgress = false

                spentTimeBaseMillis = persistedWork?.spentTime ?: 0L
                workspaceStartedAtMillis = null
                lastActivitySavedAtMillis = null
                pendingActivityEndMillis = null
                destination = MashDestination.CREATE
            }
        } else if (createdConfig == null) {
            val persistedWorkSummary = repository.getAllWorks()
                .firstOrNull { it.hasCreatedWorkConfig() }

            val persistedWork = persistedWorkSummary
                ?.let { repository.getWorkById(it.id) }
                ?: persistedWorkSummary

            val persistedConfig = persistedWork?.toMashCreateConfigOrNull()

            if (persistedWork != null && persistedConfig != null) {
                currentWorkId = persistedWork.id
                workImageBytes = null
                currentWorkImagePath = persistedWork.imagePath
                imageLoadAttemptKey = null
                createdConfig = persistedConfig

                pendingCompletedCells = persistedWork.decodeCompletedCells()
                isRestoringPersistedProgress = true

                spentTimeBaseMillis = persistedWork.spentTime ?: 0L
                workspaceStartedAtMillis = null
                lastActivitySavedAtMillis = null
                pendingActivityEndMillis = null
                destination = MashDestination.HOME
            }
        }
    }

    LaunchedEffect(currentWorkId) {
        activityStats = currentWorkId
            ?.takeIf { it > 0L }
            ?.let { workId -> repository.getWorkActivityStats(workId) }
            ?: WorkActivityStats()
    }

    LaunchedEffect(createdConfig, currentWorkImagePath, workImageBytes) {
        val config = createdConfig ?: return@LaunchedEffect

        if (workImageBytes != null) {
            return@LaunchedEffect
        }

        val loadKey = buildImageLoadKey(
            imagePath = currentWorkImagePath ?: config.imagePath,
            imageUrl = config.imageUrl,
        )

        readImageBytesFromFile(currentWorkImagePath ?: config.imagePath)?.let { imageBytes ->
            workImageBytes = imageBytes
            imageLoadAttemptKey = loadKey
            createImageLoadFailed = false
            return@LaunchedEffect
        }

        val imageUrl = config.imageUrl?.takeIf { it.isNotBlank() }
        if (imageUrl == null) {
            imageLoadAttemptKey = loadKey
            createImageLoadFailed = true
            return@LaunchedEffect
        }

        val bitmap = loadBitmapFromUrl(
            context = context,
            url = imageUrl,
        )

        if (bitmap != null) {
            workImageBytes = bitmapToByteArray(bitmap)
        }

        createImageLoadFailed = bitmap == null
        imageLoadAttemptKey = loadKey
    }

    LaunchedEffect(destination, showPopup) {
        onBottomBarVisibilityChanged((destination != MashDestination.CREATE)|| showPopup )
    }

    LaunchedEffect(destination) {
        val now = System.currentTimeMillis()
        val startedAt = workspaceStartedAtMillis

        if (destination == MashDestination.WORKSPACE && startedAt == null) {
            workspaceStartedAtMillis = now
            lastActivitySavedAtMillis = now
        } else if (destination != MashDestination.WORKSPACE && startedAt != null) {
            spentTimeBaseMillis += now - startedAt
            workspaceStartedAtMillis = null
            pendingActivityEndMillis = now
            spentTimeSaveTick++
        }
    }

    LaunchedEffect(destination, workspaceStartedAtMillis) {
        while (destination == MashDestination.WORKSPACE && workspaceStartedAtMillis != null) {
            delay(SPENT_TIME_SAVE_INTERVAL_MILLIS)
            pendingActivityEndMillis = System.currentTimeMillis()
            spentTimeSaveTick++
        }
    }

    LaunchedEffect(currentWorkId, pendingActivityEndMillis) {
        val workId = currentWorkId?.takeIf { it > 0L } ?: return@LaunchedEffect
        val activityEndMillis = pendingActivityEndMillis ?: return@LaunchedEffect
        val activityStartMillis = lastActivitySavedAtMillis ?: activityEndMillis

        if (activityEndMillis > activityStartMillis) {
            repository.addWorkActivityTime(
                workId = workId,
                startedAtMillis = activityStartMillis,
                endedAtMillis = activityEndMillis,
            )
            activityStats = repository.getWorkActivityStats(workId)
            lastActivitySavedAtMillis = activityEndMillis
        }

        pendingActivityEndMillis = null
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

    LaunchedEffect(
        createdConfig,
        uiState.scheme,
        uiState.completedCellIndices,
        spentTimeSaveTick,
        isRestoringPersistedProgress,
    ) {
        if (isRestoringPersistedProgress) {
            return@LaunchedEffect
        }

        val config = createdConfig ?: return@LaunchedEffect
        uiState.scheme ?: return@LaunchedEffect

        val workImageKey = config.imageUrl
            ?.takeIf { it.isNotBlank() }
            ?: config.imagePath?.takeIf { it.isNotBlank() }
            ?: return@LaunchedEffect

        val activeSpentTime = workspaceStartedAtMillis
            ?.let { startedAt -> System.currentTimeMillis() - startedAt }
            ?: 0L

        currentWorkId = repository.addWork(
            config.toWorkLocalItem(
                id = currentWorkId ?: 0,
                image = workImageBytes,
                isFavorite = repository.isFavorite(workImageKey),
                uiState = uiState,
                spentTimeMillis = spentTimeBaseMillis + activeSpentTime,
            ),
        )
    }

    LaunchedEffect(
        destination,
        uiState.scheme?.indices?.size,
        uiState.completedCellIndices.size,
        forceOpenCompletedWorkspace,
    ) {
        val totalCells = uiState.scheme?.indices?.size ?: 0
        val completedCells = uiState.completedCellIndices.size
        val isCompleted = totalCells in 1..completedCells

        if (!isCompleted) {
            forceOpenCompletedWorkspace = false
            return@LaunchedEffect
        }

        if (
            destination == MashDestination.WORKSPACE &&
            !forceOpenCompletedWorkspace
        ) {
            destination = MashDestination.COMPLETED
        }
    }

    BackHandler(enabled = destination != MashDestination.HOME) {
        destination = when (destination) {
            MashDestination.COMPLETED -> MashDestination.HOME
            else -> MashDestination.HOME
        }
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
                            currentWorkImagePath = null
                            imageLoadAttemptKey = null
                            pendingCompletedCells = null
                            isRestoringPersistedProgress = false
                            spentTimeBaseMillis = 0L
                            workspaceStartedAtMillis = null
                            lastActivitySavedAtMillis = null
                            pendingActivityEndMillis = null

                            viewModel.resetWork()

                            MashWorkCache.currentWork = null
                        }
                    }
                },
                onSearchFeedClick = {
                    onOpenFeed()
                }
            )
        }

        MashDestination.CREATE -> {

            if (showPopup) {
                backgroundContent()
                FeedPopupScreen(
                    imageUrl = selectedCreateImageUrl ?: createdConfig?.imageUrl ?: "",
                    onStartWork = {
                        showPopup = false
                    },
                    onClose = {
                        showPopup = false
                    },
                    onSave = {

                    }
                )
            } else {
                val createImageUrl = selectedCreateImageUrl ?: createdConfig?.imageUrl
                val createLocalImagePath = if (createImageUrl.isNullOrBlank()) {
                    currentWorkImagePath ?: createdConfig?.imagePath
                } else {
                    null
                }
                MashCreateScreen(
                    imageUrl = createImageUrl,
                    localImagePath = createLocalImagePath,
                    localImageBytes = if (createLocalImagePath.isNullOrBlank()) {
                        null
                    } else {
                        workImageBytes
                    },
                    imageLoadFailed = createImageLoadFailed,
                    onBackClick = {
                        onBack()
                    },
                    onPickLocalImage = {
                        localImagePickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                    onOpenFeed = {
                        currentWorkImagePath = null
                        workImageBytes = null
                        imageLoadAttemptKey = null
                        onOpenFeed()
                    },
                    onCreateFinished = { newConfig ->
                        forceOpenCompletedWorkspace = false
                        if (
                            createdConfig?.imageUrl != newConfig.imageUrl ||
                            createdConfig?.imagePath != newConfig.imagePath
                        ) {
                            workImageBytes = null
                            currentWorkImagePath = newConfig.imagePath
                            imageLoadAttemptKey = null
                        }

                        if (createdConfig != newConfig) {
                            spentTimeBaseMillis = 0L
                            workspaceStartedAtMillis = null
                            lastActivitySavedAtMillis = null
                            pendingActivityEndMillis = null
                        }

                        lastGeneratedConfig = null
                        createdConfig = newConfig
                        destination = MashDestination.WORKSPACE
                    }
                )
            }
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

        MashDestination.COMPLETED -> {
            WorkCompletedEntryPoint(
                imageUrl = createdConfig?.imageUrl,
                imagePath = currentWorkImagePath,
                projectTitle = createdConfig?.projectName.orEmpty(),
                onBackClick = {
                    destination = MashDestination.HOME
                },
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
                onOpenWorkClick = {
                    forceOpenCompletedWorkspace = true
                    destination = MashDestination.WORKSPACE
                },
                onRecommendationClick = { _ ->

                    currentWorkId = null
                    createdConfig = null
                    lastGeneratedConfig = null
                    workImageBytes = null
                    currentWorkImagePath = null
                    imageLoadAttemptKey = null
                    pendingCompletedCells = null
                    isRestoringPersistedProgress = false

                    spentTimeBaseMillis = 0L
                    workspaceStartedAtMillis = null
                    lastActivitySavedAtMillis = null
                    pendingActivityEndMillis = null

                    forceOpenCompletedWorkspace = false
                    viewModel.resetWork()

                    destination = MashDestination.CREATE
                },
                onSeeAllClick = {
                    onOpenFeed()
                },
            )
        }

        MashDestination.STATISTICS -> {
            MashStatisticsScreen(
                projectConfig = createdConfig,
                uiState = uiState,
                activityStats = activityStats,
                onBackClick = { destination = MashDestination.HOME },
                onCreateProjectClick = { destination = MashDestination.CREATE },
                onOpenProjectClick = { destination = MashDestination.WORKSPACE },
            )
        }
    }
}

private fun buildImageLoadKey(
    imagePath: String?,
    imageUrl: String?,
): String {
    return "${imagePath.orEmpty()}|${imageUrl.orEmpty()}"
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
