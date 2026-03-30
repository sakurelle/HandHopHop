package ru.handhophop.feature.mash

import android.app.Application
import android.graphics.Bitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val DEFAULT_MIN_SIDE_CELLS = 128
private const val DEFAULT_PALETTE_SIZE = 10
private const val DEFAULT_VISIBLE_PALETTE_SIZE = 10
private const val DEFAULT_TRANSPARENT_ALPHA_THRESHOLD = 40
private const val DEFAULT_QUANTIZE_STEP = 32
private const val DEFAULT_FALLBACK_GRID_SIZE = 128

internal class MashViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MashUiState())
    val uiState: StateFlow<MashUiState> = _uiState

    internal fun handleAction(action: UiAction) {
        when (action) {
            is ClickDownloadsAction -> download()
            is GenerateSchemeAction -> generateScheme(action.imageUrl)
            is HighlightingColorAction -> {
                // TODO делаем выделение определенного цвета
            }
            is ShadedColorAction -> {
                // TODO делаем вывод итогового результата по двойному нажатию
            }
        }
    }

    private fun generateScheme(imageUrl: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                scheme = null,
                visiblePalette = emptyList(),
                errorTextRes = null,
                isDownloadButtonEnabled = false,
                isPaletteVisible = false
            )

            if (imageUrl.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    scheme = null,
                    visiblePalette = emptyList(),
                    errorTextRes = R.string.mash_select_scheme_first,
                    isDownloadButtonEnabled = false,
                    isPaletteVisible = false
                )
                return@launch
            }

            val bitmap = loadBitmapFromUrl(
                context = getApplication<Application>().applicationContext,
                url = imageUrl
            )

            val scheme = bitmap?.let {
                buildScheme(
                    source = it,
                    minSideCells = DEFAULT_MIN_SIDE_CELLS,
                    paletteSize = DEFAULT_PALETTE_SIZE
                )
            }

            _uiState.value = if (scheme != null) {
                val visiblePalette = scheme.palette.take(DEFAULT_VISIBLE_PALETTE_SIZE)

                _uiState.value.copy(
                    isLoading = false,
                    scheme = scheme,
                    visiblePalette = visiblePalette,
                    errorTextRes = null,
                    isDownloadButtonEnabled = true,
                    isPaletteVisible = visiblePalette.isNotEmpty()
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    scheme = null,
                    visiblePalette = emptyList(),
                    errorTextRes = R.string.mash_failed_build_scheme,
                    isDownloadButtonEnabled = false,
                    isPaletteVisible = false
                )
            }
        }
    }

    private fun download() {
        // TODO: <Задача Александра с ui компонентами>
    }
}

private fun buildScheme(
    source: Bitmap,
    minSideCells: Int,
    paletteSize: Int
): SchemeData {
    val (gw, gh) = calcGridSize(source.width, source.height, minSideCells)
    val small = source.scale(gw, gh, false)

    val palette = extractTopColorsFromSmallBitmap(
        bmp = small,
        topN = paletteSize,
        step = DEFAULT_QUANTIZE_STEP
    )
    val paletteInts = palette.map { it.toArgbInt() }

    val indices = IntArray(gw * gh)

    for (y in 0 until gh) {
        for (x in 0 until gw) {
            val c = small[x, y]
            val a = (c ushr 24) and 0xFF

            indices[y * gw + x] =
                if (a < DEFAULT_TRANSPARENT_ALPHA_THRESHOLD) {
                    nearestColorIndex(0xFFFFFFFF.toInt(), paletteInts)
                } else {
                    nearestColorIndex(c, paletteInts)
                }
        }
    }

    return SchemeData(
        gridW = gw,
        gridH = gh,
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
    if (minDim <= 0) return DEFAULT_FALLBACK_GRID_SIZE to DEFAULT_FALLBACK_GRID_SIZE

    val scale = minSide.toFloat() / minDim.toFloat()
    val nw = max(1, (w * scale).roundToInt())
    val nh = max(1, (h * scale).roundToInt())

    return nw to nh
}