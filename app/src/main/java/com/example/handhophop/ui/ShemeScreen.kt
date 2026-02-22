package com.example.handhophop.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.core.graphics.get
import androidx.core.graphics.createBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.handhophop.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun ShemeScreen(
    navController: NavHostController,
    selectedVm: SelectedSchemeViewModel
) {
    val bg = colorResource(R.color.bg_beige)
    val selectedUrl by selectedVm.selectedUrl.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        BackgroundPattern()

        Column(modifier = Modifier.fillMaxSize()) {
            TopBanner(title = stringResource(R.string.home_title_incomplete))
            CenterContentScheme(imageUrl = selectedUrl)
            BottomBar(navController = navController, modifier = Modifier.fillMaxWidth())
        }
    }
}

/**
 * ✅ Скроллимый экран схемы:
 * - схема может быть большой
 * - ниже всегда доступны "Скачать" и палитра
 * - bitmap загружается 1 раз, схема строится 1 раз
 */
@Composable
private fun ColumnScope.CenterContentScheme(imageUrl: String?) {
    val sidePad = dimensionResource(id = R.dimen.screen_side_padding)
    val context = LocalContext.current
    val key = imageUrl ?: "drawable:${R.drawable.project_preview}"
    val gap = dimensionResource(R.dimen.center_block_gap)

    var loading by remember(key) { mutableStateOf(true) }
    var scheme by remember(key) { mutableStateOf<SchemeData?>(null) }
    var error by remember(key) { mutableStateOf<String?>(null) }

    LaunchedEffect(key) {
        loading = true
        error = null
        scheme = null

        val bmp = if (imageUrl != null) {
            loadBitmapFromUrl(context, imageUrl)
        } else {
            loadBitmapFromDrawable(context, R.drawable.project_preview)
        }

        scheme = if (bmp != null) {
            buildScheme(source = bmp, minSideCells = 128, paletteSize = 10)
        } else null

        if (scheme == null) error = "Не удалось построить схему"
        loading = false
    }

    LazyColumn(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = sidePad),
        verticalArrangement = Arrangement.spacedBy(gap),
        contentPadding = PaddingValues(vertical = gap)
    ) {
        item {
            SchemeCard(
                loading = loading,
                scheme = scheme,
                error = error
            )
        }

        item {
            DownloadButton(enabled = (scheme != null && !loading))
        }

        item {
            scheme?.let { PaletteBar(it) }
        }
    }
}

@Composable
private fun SchemeCard(
    loading: Boolean,
    scheme: SchemeData?,
    error: String?
) {
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)
    val pad = dimensionResource(R.dimen.preview_text_h_padding)
    val outerColor = colorResource(R.color.card_beige)

    val aspect = scheme?.let { it.gridW.toFloat() / it.gridH.toFloat() } ?: 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = pad),
        shape = RoundedCornerShape(r),
        colors = CardDefaults.cardColors(containerColor = outerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation0)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspect),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading -> CircularProgressIndicator()
                scheme != null -> NumberedSchemeCanvas(scheme = scheme)
                else -> Text(
                    text = error ?: "Нет схемы",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun DownloadButton(enabled: Boolean) {
    val h = dimensionResource(R.dimen.stats_button_height)
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)
    val bg = colorResource(R.color.primary_brown)

    Button(
        onClick = { /* TODO: сохранить в Downloads */ },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(h),
        shape = RoundedCornerShape(r),
        colors = ButtonDefaults.buttonColors(containerColor = bg),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = elevation0)
    ) {
        Text(
            text = stringResource(R.string.download_scheme),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PaletteBar(scheme: SchemeData) {
    val titleColor = colorResource(R.color.text_dark)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.color_palette),
            color = titleColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            scheme.palette.take(10).forEachIndexed { index, c ->
                ColorSwatch(color = c, number = index + 1)
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, number: Int) {
    val size = 32.dp
    val fontSize = 10.sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(color, RoundedCornerShape(4.dp))
        )
        Text(
            text = number.toString(),
            fontSize = fontSize,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}

/**
 * ✅ Схема "сеткой по номерам" + zoom/pan.
 *
 * Важно:
 * - при scale == 1 и 1 пальце мы НЕ перехватываем жест → можно скроллить страницу
 * - zoom/pan включается при 2 пальцах (или если scale>1)
 * - трансформация через graphicsLayer (стабильно)
 */
@Composable
private fun NumberedSchemeCanvas(scheme: SchemeData) {
    var scale by remember(scheme.gridW, scheme.gridH, scheme.indices.size) { mutableStateOf(1f) }
    var offset by remember(scheme.gridW, scheme.gridH, scheme.indices.size) { mutableStateOf(Offset.Zero) }

    val minScale = 1f
    val maxScale = 6f

    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    fun clampOffset(viewW: Float, viewH: Float, sc: Float, off: Offset): Offset {
        val scaledW = viewW * sc
        val scaledH = viewH * sc
        val minX = min(0f, viewW - scaledW)
        val minY = min(0f, viewH - scaledH)
        return Offset(
            x = off.x.coerceIn(minX, 0f),
            y = off.y.coerceIn(minY, 0f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color.White)
            .pointerInput(scheme.gridW, scheme.gridH, scheme.indices.size) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)

                    var active = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.any { it.pressed }
                        if (!pressed) break

                        val pointers = event.changes.count { it.pressed }

                        // ✅ если scale==1 и 1 палец — не активируем, пусть LazyColumn скроллит
                        if (!active) {
                            if (pointers >= 2 || scale > 1f) {
                                active = true
                            } else {
                                // не consume — отдаём событие родителю (скроллу)
                                continue
                            }
                        }

                        // активное управление схемой
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()
                        val centroid = event.calculateCentroid()

                        val viewW = size.width.toFloat()
                        val viewH = size.height.toFloat()

                        val oldScale = scale
                        val newScale = (oldScale * zoom).coerceIn(minScale, maxScale)
                        val r = newScale / oldScale

                        val newOffset = (offset - centroid) * r + centroid + pan

                        scale = newScale
                        offset = clampOffset(viewW, viewH, newScale, newOffset)

                        // ✅ потребляем изменения, чтобы скролл не боролся с паном
                        event.changes.forEach { ch ->
                            if (ch.positionChanged()) ch.consume()
                        }
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offset.x
                    translationY = offset.y
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0f, 0f)
                }
        ) {
            val w = scheme.gridW
            val h = scheme.gridH

            // при aspectRatio карточки сетка заполняет canvas корректно
            val cell = size.width / w.toFloat()

            // видимая область
            val left = (-offset.x) / scale
            val top = (-offset.y) / scale
            val right = (size.width - offset.x) / scale
            val bottom = (size.height - offset.y) / scale

            val x0 = floor(left / cell).toInt().coerceIn(0, w - 1)
            val y0 = floor(top / cell).toInt().coerceIn(0, h - 1)
            val x1 = ceil(right / cell).toInt().coerceIn(0, w - 1)
            val y1 = ceil(bottom / cell).toInt().coerceIn(0, h - 1)

            val effectiveCellPx = cell * scale
            val drawNumbers = effectiveCellPx >= 18f
            val gridStroke = Stroke(width = (1f / scale).coerceAtLeast(0.5f))

            for (yy in y0..y1) {
                val topY = yy * cell
                for (xx in x0..x1) {
                    val i = yy * w + xx
                    val idx = scheme.indices[i]
                    val color = scheme.palette[idx]

                    val leftX = xx * cell
                    val rectSize = Size(cell, cell)

                    // легкая заливка
                    drawRect(
                        color = color.copy(alpha = 0.18f),
                        topLeft = Offset(leftX, topY),
                        size = rectSize
                    )

                    // сетка
                    drawRect(
                        color = Color.Black.copy(alpha = 0.25f),
                        topLeft = Offset(leftX, topY),
                        size = rectSize,
                        style = gridStroke
                    )

                    // номер
                    if (drawNumbers) {
                        val number = (idx + 1).toString()
                        textPaint.color = Color.Black.copy(alpha = 0.75f).toArgb()
                        textPaint.textSize = cell * 0.55f

                        val cx = leftX + cell / 2f
                        val cy = topY + cell / 2f - (textPaint.ascent() + textPaint.descent()) / 2f
                        drawContext.canvas.nativeCanvas.drawText(number, cx, cy, textPaint)
                    }
                }
            }
        }
    }
}

// -------------------------
// Scheme data + build
// -------------------------
private data class SchemeData(
    val gridW: Int,
    val gridH: Int,
    val palette: List<Color>,   // size K
    val indices: IntArray       // size gridW*gridH, each 0..K-1
)

private fun buildScheme(
    source: Bitmap,
    minSideCells: Int,
    paletteSize: Int
): SchemeData {
    val (gw, gh) = calcGridSize(source.width, source.height, minSideCells)
    val small = Bitmap.createScaledBitmap(source, gw, gh, false)

    val palette = extractTopColorsFromSmallBitmap(small, topN = paletteSize, step = 32)
    val paletteInts = palette.map { it.toArgb() }

    val indices = IntArray(gw * gh)
    for (y in 0 until gh) {
        for (x in 0 until gw) {
            val c = small[x, y]
            val a = (c ushr 24) and 0xFF
            indices[y * gw + x] =
                if (a < 40) nearestColorIndex(0xFFFFFFFF.toInt(), paletteInts)
                else nearestColorIndex(c, paletteInts)
        }
    }

    return SchemeData(
        gridW = gw,
        gridH = gh,
        palette = palette,
        indices = indices
    )
}

private fun calcGridSize(w: Int, h: Int, minSide: Int): Pair<Int, Int> {
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
): List<Color> {
    val counts = HashMap<Int, Int>(4096)
    val w = bmp.width
    val h = bmp.height

    for (y in 0 until h) {
        for (x in 0 until w) {
            val c = bmp[x, y]
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
        .map { Color(it.key) }

    return if (sorted.isNotEmpty()) sorted else listOf(
        Color(0xFF000000),
        Color(0xFFFFFFFF),
        Color(0xFF7F7F7F)
    ).take(topN)
}

private fun nearestColorIndex(argb: Int, palette: List<Int>): Int {
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

private fun quantize(v: Int, step: Int): Int {
    val q = (v / step) * step
    return min(255, max(0, q))
}

// -------------------------
// Bitmap loading helpers
// -------------------------
private suspend fun loadBitmapFromUrl(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()

        val result = loader.execute(request)
        if (result is SuccessResult) {
            drawableToBitmap(result.drawable)
        } else null
    } catch (_: Exception) {
        null
    }
}

private suspend fun loadBitmapFromDrawable(context: Context, @DrawableRes resId: Int): Bitmap? =
    withContext(Dispatchers.IO) {
        try {
            BitmapFactory.decodeResource(context.resources, resId)
        } catch (_: Exception) {
            null
        }
    }

private fun drawableToBitmap(drawable: android.graphics.drawable.Drawable): Bitmap? {
    return try {
        if (drawable is android.graphics.drawable.BitmapDrawable) {
            drawable.bitmap
        } else {
            val w = max(1, drawable.intrinsicWidth)
            val h = max(1, drawable.intrinsicHeight)
            val bitmap = createBitmap(w, h)
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    } catch (_: Exception) {
        null
    }
}