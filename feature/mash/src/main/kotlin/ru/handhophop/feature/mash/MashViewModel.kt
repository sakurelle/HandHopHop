package ru.handhophop.feature.mash

import android.graphics.Bitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.handhophop.feature.mash.MashCreate.MashCreateConfig
import ru.handhophop.feature.mash.MashCreate.MashCreateData
import ru.handhophop.feature.mash.MashCreate.MashThread
import ru.handhophop.feature.mash.Statistics.buildPaletteProgress
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val DEFAULT_TRANSPARENT_ALPHA_THRESHOLD = 40
private const val DEFAULT_FALLBACK_GRID_SIZE = 128

internal sealed interface MashEvent {

    data class ExportPdf(
        val projectTitle: String,
        val scheme: SchemeData,
    ) : MashEvent
}

internal class MashViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MashUiState())
    val uiState: StateFlow<MashUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MashEvent>()
    val events: SharedFlow<MashEvent> = _events.asSharedFlow()

    internal fun handleAction(action: UiAction) {
        when (action) {
            is ClickDownloadsAction -> requestPdfExport(action.projectTitle)

            is GenerateSchemeAction -> generateScheme(
                config = action.config,
                imageBytes = action.imageBytes,
                initialCompletedCellIndices = action.initialCompletedCellIndices,
            )

            is ClickSchemeCellAction -> handleSchemeCellClick(action.cellIndex)

            is TogglePaletteHighlightAction -> {
                val currentSelected = _uiState.value.selectedPaletteIndex
                _uiState.value = _uiState.value.copy(
                    selectedPaletteIndex = if (currentSelected == action.paletteIndex) {
                        null
                    } else {
                        action.paletteIndex
                    },
                )
            }

            is ClearPaletteHighlightAction -> {
                _uiState.value = _uiState.value.copy(
                    selectedPaletteIndex = null,
                )
            }

            is TogglePaletteCompletedAction -> {
                togglePaletteCells(action.paletteIndex)
            }

        }
    }

    internal fun resetWork() {
        _uiState.value = MashUiState(
            isLoading = false,
            scheme = null,
            visiblePalette = emptyList(),
            paletteProgress = emptyList(),
            errorTextRes = null,
            isDownloadButtonEnabled = false,
            isPaletteVisible = false,
            selectedPaletteIndex = null,
            completedCellIndices = emptySet()
        )
    }

    internal fun restoreCachedWork(cachedState: MashUiState) {
        _uiState.value = cachedState.withDerivedPaletteState()
    }

    internal fun restoreCompletedCells(completedCellIndices: Set<Int>) {
        val currentScheme = _uiState.value.scheme ?: return

        val sanitizedIndices = completedCellIndices.filterTo(linkedSetOf()) { index ->
            index in currentScheme.indices.indices
        }

        _uiState.value = _uiState.value.copy(
            completedCellIndices = sanitizedIndices,
        ).withDerivedPaletteState()
    }

    private fun handleSchemeCellClick(cellIndex: Int) {
        val currentState = _uiState.value
        val currentScheme = currentState.scheme ?: return

        if (cellIndex !in currentScheme.indices.indices) {
            return
        }

        val paletteIndex = currentScheme.indices[cellIndex]

        if (currentState.selectedPaletteIndex != paletteIndex) {
            _uiState.value = currentState.copy(
                selectedPaletteIndex = paletteIndex,
            )
            return
        }

        val completedCells = currentState.completedCellIndices.toMutableSet()
        val isCompleted = completedCells.add(cellIndex)

        if (!isCompleted) {
            completedCells.remove(cellIndex)
        }

        _uiState.value = currentState.copy(
            completedCellIndices = completedCells,
        ).withDerivedPaletteState()
    }

    private fun togglePaletteCells(paletteIndex: Int) {
        val currentState = _uiState.value
        val currentScheme = currentState.scheme ?: return

        val cellIndices = currentScheme.indices.indices
            .filter { currentScheme.indices[it] == paletteIndex }

        if (cellIndices.isEmpty()) {
            return
        }

        val completedCells = currentState.completedCellIndices.toMutableSet()
        val shouldMarkCompleted = cellIndices.any { it !in completedCells }

        cellIndices.forEach { cellIndex ->
            if (shouldMarkCompleted) {
                completedCells.add(cellIndex)
            } else {
                completedCells.remove(cellIndex)
            }
        }

        _uiState.value = currentState.copy(
            completedCellIndices = completedCells,
        ).withDerivedPaletteState()
    }

    private fun generateScheme(
        config: MashCreateConfig,
        imageBytes: ByteArray?,
        initialCompletedCellIndices: Set<Int>,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                scheme = null,
                visiblePalette = emptyList(),
                paletteProgress = emptyList(),
                errorTextRes = null,
                isDownloadButtonEnabled = false,
                isPaletteVisible = false,
                selectedPaletteIndex = null,
                completedCellIndices = emptySet(),
            )

            val scheme = withContext(Dispatchers.Default) {
                imageBytes
                    ?.let(::byteArrayToBitmap)
                    ?.let { bitmap ->
                        buildScheme(
                            source = bitmap,
                            minSideCells = config.difficulty.minSidePx,
                            availablePalette = MashCreateData.allThreads,
                            maxColors = config.colorCount,
                        )
                    }
            }

            _uiState.value = if (scheme != null) {
                val restoredCompletedCells = initialCompletedCellIndices
                    .filterTo(linkedSetOf()) { index ->
                        index in scheme.indices.indices
                    }

                _uiState.value.copy(
                    isLoading = false,
                    scheme = scheme,
                    errorTextRes = null,
                    selectedPaletteIndex = null,
                    completedCellIndices = restoredCompletedCells,
                ).withDerivedPaletteState()
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    scheme = null,
                    visiblePalette = emptyList(),
                    paletteProgress = emptyList(),
                    errorTextRes = R.string.mash_failed_build_scheme,
                    isDownloadButtonEnabled = false,
                    isPaletteVisible = false,
                    selectedPaletteIndex = null,
                    completedCellIndices = emptySet(),
                )
            }
        }
    }

    private fun requestPdfExport(projectTitle: String) {
        val currentScheme = _uiState.value.scheme ?: return

        viewModelScope.launch {
            _events.emit(
                MashEvent.ExportPdf(
                    projectTitle = projectTitle,
                    scheme = currentScheme,
                ),
            )
        }
    }

    private fun MashUiState.withDerivedPaletteState(): MashUiState {
        val currentScheme = scheme ?: return copy(
            visiblePalette = emptyList(),
            paletteProgress = emptyList(),
            isPaletteVisible = false,
            isDownloadButtonEnabled = false,
        )

        val currentPaletteProgress = currentScheme.buildPaletteProgress(completedCellIndices)

        val paletteWithCompletion = currentScheme.palette.mapIndexed { index, thread ->
            thread.copy(
                isCompleted = currentPaletteProgress.getOrNull(index)?.isCompleted == true,
            )
        }

        return copy(
            visiblePalette = paletteWithCompletion,
            paletteProgress = currentPaletteProgress,
            isPaletteVisible = paletteWithCompletion.isNotEmpty(),
            isDownloadButtonEnabled = true,
        )
    }
}

private fun buildScheme(
    source: Bitmap,
    minSideCells: Int,
    availablePalette: List<MashThread>,
    maxColors: Int,
): SchemeData {
    val selectedPalette = selectPaletteForImage(
        source = source,
        minSideCells = minSideCells,
        availablePalette = availablePalette,
        maxColors = maxColors,
    )

    val (gridWidth, gridHeight) = calcGridSize(
        w = source.width,
        h = source.height,
        minSide = minSideCells,
    )

    val scaledBitmap = source.scale(gridWidth, gridHeight, false)
    val selectedPaletteInts = selectedPalette.map { it.color.toArgbInt() }
    val indices = IntArray(gridWidth * gridHeight)

    for (y in 0 until gridHeight) {
        for (x in 0 until gridWidth) {
            val color = scaledBitmap[x, y]
            val alpha = (color ushr 24) and 0xFF

            indices[y * gridWidth + x] = if (alpha < DEFAULT_TRANSPARENT_ALPHA_THRESHOLD) {
                nearestColorIndex(0xFFFFFFFF.toInt(), selectedPaletteInts)
            } else {
                nearestColorIndex(color, selectedPaletteInts)
            }
        }
    }

    return SchemeData(
        gridW = gridWidth,
        gridH = gridHeight,
        palette = selectedPalette,
        indices = indices,
    )
}

internal fun selectPaletteForImage(
    source: Bitmap,
    minSideCells: Int,
    availablePalette: List<MashThread>,
    maxColors: Int,
): List<MashThread> {
    val (gridWidth, gridHeight) = calcGridSize(
        w = source.width,
        h = source.height,
        minSide = minSideCells,
    )

    val scaledBitmap = source.scale(gridWidth, gridHeight, false)
    val paletteInts = availablePalette.map { it.color.toArgbInt() }
    val paletteUsage = IntArray(availablePalette.size)

    for (y in 0 until gridHeight) {
        for (x in 0 until gridWidth) {
            val color = scaledBitmap[x, y]
            val alpha = (color ushr 24) and 0xFF

            val nearestIndex = if (alpha < DEFAULT_TRANSPARENT_ALPHA_THRESHOLD) {
                nearestColorIndex(0xFFFFFFFF.toInt(), paletteInts)
            } else {
                nearestColorIndex(color, paletteInts)
            }

            paletteUsage[nearestIndex]++
        }
    }

    val normalizedMaxColors = maxColors.coerceIn(1, availablePalette.size)

    val selectedPaletteSourceIndices = paletteUsage.indices
        .filter { paletteUsage[it] > 0 }
        .sortedWith(
            compareByDescending<Int> { paletteUsage[it] }
                .thenBy { it },
        )
        .take(normalizedMaxColors)
        .sorted()

    val effectiveSourceIndices = selectedPaletteSourceIndices.ifEmpty {
        availablePalette.indices.take(normalizedMaxColors)
    }

    return effectiveSourceIndices.map(availablePalette::get)
}

private fun calcGridSize(
    w: Int,
    h: Int,
    minSide: Int,
): Pair<Int, Int> {
    val minDim = min(w, h)

    if (minDim <= 0) {
        return DEFAULT_FALLBACK_GRID_SIZE to DEFAULT_FALLBACK_GRID_SIZE
    }

    val scale = minSide.toFloat() / minDim.toFloat()
    val newWidth = max(1, (w * scale).roundToInt())
    val newHeight = max(1, (h * scale).roundToInt())

    return newWidth to newHeight
}