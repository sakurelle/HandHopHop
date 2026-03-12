package com.example.handhophop.feature.mash.presentation

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal class MashViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MashUiState())
    internal val uiState: StateFlow<MashUiState> = _uiState

    internal fun handleAction(action: UiAction) {
        when (action) {
            is ClickDownloadsAction -> {
                download()
            }

            is GenerateShemaAction -> {
                generateScheme(action.imageUrl)
            }
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
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    scheme = null,
                    error = "Не удалось построить схему"
                )
            }
        }
    }

    private fun download() {
        // TODO сохранение схемы в Downloads
    }
}

private suspend fun loadBitmapFromUrl(
    context: android.content.Context,
    url: String
): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()

        val result = loader.execute(request)
        if (result is SuccessResult) {
            val drawable = result.drawable
            if (drawable is android.graphics.drawable.BitmapDrawable) {
                drawable.bitmap
            } else {
                val w = max(1, drawable.intrinsicWidth)
                val h = max(1, drawable.intrinsicHeight)
                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

private fun buildScheme(
    source: Bitmap,
    minSideCells: Int,
    paletteSize: Int
): SchemeData {
    val (gw, gh) = calcGridSize(source.width, source.height, minSideCells)
    val small = Bitmap.createScaledBitmap(source, gw, gh, false)

    val palette = extractTopColorsFromSmallBitmap(
        bmp = small,
        topN = paletteSize,
        step = 32
    )
    val paletteInts = palette.map { it.toArgb() }

    val indices = IntArray(gw * gh)

    for (y in 0 until gh) {
        for (x in 0 until gw) {
            val c = small.getPixel(x, y)
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

private fun extractTopColorsFromSmallBitmap(
    bmp: Bitmap,
    topN: Int,
    step: Int
): List<androidx.compose.ui.graphics.Color> {
    val counts = HashMap<Int, Int>(4096)
    val w = bmp.width
    val h = bmp.height

    for (y in 0 until h) {
        for (x in 0 until w) {
            val c = bmp.getPixel(x, y)
            val a = (c ushr 24) and 0xFF
            if (a < 40) continue

            val r = (c ushr 16) and 0xFF
            val g = (c ushr 8) and 0xFF
            val b = c and 0xFF

            val rq = quantize(r, step)
            val gq = quantize(g, step)
            val bq = quantize(b, step)

            val packed = (0xFF shl 24) or (rq shl 16) or (gq shl 8) or bq
            counts[packed] = (counts[packed] ?: 0) + 1
        }
    }

    val sorted = counts.entries
        .sortedByDescending { it.value }
        .take(topN)
        .map { androidx.compose.ui.graphics.Color(it.key) }

    return if (sorted.isNotEmpty()) {
        sorted
    } else {
        listOf(
            androidx.compose.ui.graphics.Color(0xFF000000),
            androidx.compose.ui.graphics.Color(0xFFFFFFFF),
            androidx.compose.ui.graphics.Color(0xFF7F7F7F)
        ).take(topN)
    }
}

private fun nearestColorIndex(
    argb: Int,
    palette: List<Int>
): Int {
    val r = (argb ushr 16) and 0xFF
    val g = (argb ushr 8) and 0xFF
    val b = argb and 0xFF

    var best = 0
    var bestD = Int.MAX_VALUE

    for (i in palette.indices) {
        val p = palette[i]
        val pr = (p ushr 16) and 0xFF
        val pg = (p ushr 8) and 0xFF
        val pb = p and 0xFF

        val dr = r - pr
        val dg = g - pg
        val db = b - pb
        val d = dr * dr + dg * dg + db * db

        if (d < bestD) {
            bestD = d
            best = i
        }
    }

    return best
}

private fun quantize(
    v: Int,
    step: Int
): Int {
    val q = (v / step) * step
    return min(255, max(0, q))
}

private fun androidx.compose.ui.graphics.Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}