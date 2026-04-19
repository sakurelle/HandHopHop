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
import ru.handhophop.feature.mash.MashCreate.MashCreateConfig
import ru.handhophop.feature.mash.MashCreate.MashThread
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
            is GenerateSchemeAction -> generateScheme(action.config)
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
        )
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
        )
    }

    private fun generateScheme(config: MashCreateConfig) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                scheme = null,
                visiblePalette = emptyList(),
                errorTextRes = null,
                isDownloadButtonEnabled = false,
                isPaletteVisible = false,
                selectedPaletteIndex = null,
                completedCellIndices = emptySet(),
            )

            val context = getApplication<Application>().applicationContext

            val bitmap = loadBitmapFromUrl(
                context = context,
                url = config.imageUrl ?: DEFAULT_MASH_IMAGE_URL
            )

            val scheme = bitmap?.let {
                buildScheme(
                    source = it,
                    minSideCells = config.difficulty.minSidePx,
                    palette = config.threads
                )
            }

            _uiState.value = if (scheme != null) {
                _uiState.value.copy(
                    isLoading = false,
                    scheme = scheme,
                    visiblePalette = scheme.palette,
                    errorTextRes = null,
                    isDownloadButtonEnabled = true,
                    isPaletteVisible = scheme.palette.isNotEmpty(),
                    selectedPaletteIndex = null,
                    completedCellIndices = emptySet(),
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    scheme = null,
                    visiblePalette = emptyList(),
                    errorTextRes = R.string.mash_failed_build_scheme,
                    isDownloadButtonEnabled = false,
                    isPaletteVisible = false,
                    selectedPaletteIndex = null,
                    completedCellIndices = emptySet(),
                )
            }
        }
    }

    private fun download() {
        // TODO: скачивание схемы
    }
}

private fun buildScheme(
    source: Bitmap,
    minSideCells: Int,
    palette: List<MashThread>
): SchemeData {
    val (gridWidth, gridHeight) = calcGridSize(
        w = source.width,
        h = source.height,
        minSide = minSideCells
    )
    val scaledBitmap = source.scale(gridWidth, gridHeight, false)

    val paletteInts = palette.map { it.color.toArgbInt() }
    val indices = IntArray(gridWidth * gridHeight)

    for (y in 0 until gridHeight) {
        for (x in 0 until gridWidth) {
            val color = scaledBitmap[x, y]
            val alpha = (color ushr 24) and 0xFF

            indices[y * gridWidth + x] =
                if (alpha < DEFAULT_TRANSPARENT_ALPHA_THRESHOLD) {
                    nearestColorIndex(0xFFFFFFFF.toInt(), paletteInts)
                } else {
                    nearestColorIndex(color, paletteInts)
                }
        }
    }

    return SchemeData(
        gridW = gridWidth,
        gridH = gridHeight,
        palette = palette,
        indices = indices
    )
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
