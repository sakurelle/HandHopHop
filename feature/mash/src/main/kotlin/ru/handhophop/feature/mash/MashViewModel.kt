package ru.handhophop.feature.mash

import android.app.Application
import android.graphics.Bitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.handhophop.feature.mash.MashCreate.MashCreateData
import ru.handhophop.feature.mash.MashCreate.MashCreateConfig
import ru.handhophop.feature.mash.MashCreate.MashThread
import ru.handhophop.feature.mash.Statistics.buildPaletteProgress
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val DEFAULT_TRANSPARENT_ALPHA_THRESHOLD = 40
private const val DEFAULT_FALLBACK_GRID_SIZE = 128
private const val DEFAULT_MASH_IMAGE_URL =
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQDqAZfJ7DSp_ML801Txp-yEJ5zTXIDtbM9AQ&s"

internal class MashViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MashUiState())
    val uiState: StateFlow<MashUiState> = _uiState.asStateFlow()

    internal fun handleAction(action: UiAction) {
        when (action) {
            is ClickDownloadsAction -> download()
            is GenerateSchemeAction -> generateScheme(
                config = action.config,
                imageBytes = action.imageBytes,
            )
            is ClickSchemeCellAction -> handleSchemeCellClick(action.cellIndex)

            is TogglePaletteHighlightAction -> {
                val currentSelected = _uiState.value.selectedPaletteIndex
                _uiState.value = _uiState.value.copy(
                    selectedPaletteIndex = if (currentSelected == action.paletteIndex) {
                        null
                    } else {
                        action.paletteIndex
                    }
                )
            }

            is ClearPaletteHighlightAction -> {
                _uiState.value = _uiState.value.copy(
                    selectedPaletteIndex = null
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
            _uiState.value = currentState.copy(selectedPaletteIndex = paletteIndex)
            return
        }

        val completedCells = currentState.completedCellIndices.toMutableSet()
        val isCompleted = completedCells.add(cellIndex)
        if (!isCompleted) {
            completedCells.remove(cellIndex)
        }

        _uiState.value = currentState.copy(
            completedCellIndices = completedCells
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
            completedCellIndices = completedCells
        ).withDerivedPaletteState()
    }

    private fun generateScheme(
        config: MashCreateConfig,
        imageBytes: ByteArray? = null,
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

            val context = getApplication<Application>().applicationContext

            val bitmap = imageBytes
                ?.let(::byteArrayToBitmap)
                ?: loadBitmapFromUrl(
                    context = context,
                    url = config.imageUrl ?: DEFAULT_MASH_IMAGE_URL
                )

            val scheme = bitmap?.let {
                buildScheme(
                    source = it,
                    minSideCells = config.difficulty.minSidePx,
                    availablePalette = MashCreateData.allThreads,
                    maxColors = config.colorCount,
                )
            }

            _uiState.value = if (scheme != null) {
                _uiState.value.copy(
                    isLoading = false,
                    scheme = scheme,
                    errorTextRes = null,
                    selectedPaletteIndex = null,
                    completedCellIndices = emptySet(),
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

    private fun download() {
        // TODO: скачивание схемы
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
        minSide = minSideCells
    )
    val scaledBitmap = source.scale(gridWidth, gridHeight, false)
    val selectedPaletteInts = selectedPalette.map { it.color.toArgbInt() }
    val indices = IntArray(gridWidth * gridHeight)

    for (y in 0 until gridHeight) {
        for (x in 0 until gridWidth) {
            val color = scaledBitmap[x, y]
            val alpha = (color ushr 24) and 0xFF

            indices[y * gridWidth + x] =
                if (alpha < DEFAULT_TRANSPARENT_ALPHA_THRESHOLD) {
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
        indices = indices
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
        minSide = minSideCells
    )
    val scaledBitmap = source.scale(gridWidth, gridHeight, false)
    val paletteInts = availablePalette.map { it.color.toArgbInt() }
    val paletteUsage = IntArray(availablePalette.size)

    for (y in 0 until gridHeight) {
        for (x in 0 until gridWidth) {
            val color = scaledBitmap[x, y]
            val alpha = (color ushr 24) and 0xFF

            val nearestIndex =
                if (alpha < DEFAULT_TRANSPARENT_ALPHA_THRESHOLD) {
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
                .thenBy { it }
        )
        .take(normalizedMaxColors)
        .sorted()

    val effectiveSourceIndices = if (selectedPaletteSourceIndices.isNotEmpty()) {
        selectedPaletteSourceIndices
    } else {
        availablePalette.indices.take(normalizedMaxColors)
    }

    return effectiveSourceIndices.map(availablePalette::get)
}

private fun calcGridSize(
    w: Int,
    h: Int,
    minSide: Int
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
