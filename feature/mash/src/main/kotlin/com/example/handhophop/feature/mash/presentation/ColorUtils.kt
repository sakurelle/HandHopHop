package com.example.handhophop.feature.mash.presentation

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import androidx.core.graphics.get

internal fun extractTopColorsFromSmallBitmap(
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

    return sorted.ifEmpty {
        listOf(
            Color(0xFF000000),
            Color(0xFFFFFFFF),
            Color(0xFF7F7F7F)
        ).take(topN)
    }
}

internal fun nearestColorIndex(
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

private fun quantize(v: Int, step: Int): Int {
    val q = (v / step) * step
    return min(255, max(0, q))
}

internal fun Color.toArgbInt(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}