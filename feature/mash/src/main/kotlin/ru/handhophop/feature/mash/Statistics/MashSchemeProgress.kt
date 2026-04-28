package ru.handhophop.feature.mash.Statistics

import ru.handhophop.feature.mash.SchemeData

internal data class MashPaletteProgress(
    val totalCells: Int = 0,
    val completedCells: Int = 0,
) {
    val isUsed: Boolean
        get() = totalCells > 0

    val isCompleted: Boolean
        get() = isUsed && completedCells >= totalCells
}

internal fun SchemeData.buildPaletteProgress(
    completedCellIndices: Set<Int>,
): List<MashPaletteProgress> {
    val totalUsage = IntArray(palette.size)
    val completedUsage = IntArray(palette.size)

    indices.forEachIndexed { cellIndex, paletteIndex ->
        if (paletteIndex in palette.indices) {
            totalUsage[paletteIndex]++
            if (cellIndex in completedCellIndices) {
                completedUsage[paletteIndex]++
            }
        }
    }

    return List(palette.size) { index ->
        MashPaletteProgress(
            totalCells = totalUsage[index],
            completedCells = completedUsage[index],
        )
    }
}
