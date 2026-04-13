package ru.handhophop.feature.mash

import androidx.compose.ui.graphics.Color

internal fun nearestColorIndex(
    argb: Int,
    palette: List<Int>
): Int {
    val r = (argb ushr 16) and 0xFF
    val g = (argb ushr 8) and 0xFF
    val b = argb and 0xFF

    var best = 0
    var bestDistance = Int.MAX_VALUE

    for (i in palette.indices) {
        val paletteColor = palette[i]
        val pr = (paletteColor ushr 16) and 0xFF
        val pg = (paletteColor ushr 8) and 0xFF
        val pb = paletteColor and 0xFF

        val dr = r - pr
        val dg = g - pg
        val db = b - pb
        val distance = dr * dr + dg * dg + db * db

        if (distance < bestDistance) {
            bestDistance = distance
            best = i
        }
    }

    return best
}

internal fun Color.toArgbInt(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}