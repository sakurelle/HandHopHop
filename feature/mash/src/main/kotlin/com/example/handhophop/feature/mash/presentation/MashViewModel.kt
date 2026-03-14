package com.example.handhophop.feature.mash.presentation

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import androidx.core.graphics.scale
import androidx.core.graphics.get

internal class MashViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MashUiState())
    val uiState: StateFlow<MashUiState> = _uiState

    internal fun handleAction(action: UiAction) {
        when (action) {
            is ClickDownloadsAction -> download()
            is GenerateShemaAction -> generateScheme(action.imageUrl)
        }
    }

    private fun generateScheme(imageUrl: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                imageUrl = imageUrl,
                scheme = null,
                error = null
            )

            if (imageUrl.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    scheme = null,
                    visiblePalette = emptyList(),
                    error = "Сначала выберите схему"
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
                    minSideCells = 128,
                    paletteSize = 10
                )
            }

            _uiState.value = if (scheme != null) {
                _uiState.value.copy(
                    isLoading = false,
                    scheme = scheme,
                    visiblePalette = scheme.palette.take(10),
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    scheme = null,
                    visiblePalette = emptyList(),
                    error = "Не удалось построить схему"
                )
            }
        }
    }

    private fun download() {
        // TODO скачать схему
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
        step = 32
    )
    val paletteInts = palette.map { it.toArgbInt() }

    val indices = IntArray(gw * gh)

    for (y in 0 until gh) {
        for (x in 0 until gw) {
            val c = small[x, y]
            val a = (c ushr 24) and 0xFF

            indices[y * gw + x] =
                if (a < 40) {
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
    if (minDim <= 0) return 128 to 128

    val scale = minSide.toFloat() / minDim.toFloat()
    val nw = max(1, (w * scale).roundToInt())
    val nh = max(1, (h * scale).roundToInt())

    return nw to nh
}