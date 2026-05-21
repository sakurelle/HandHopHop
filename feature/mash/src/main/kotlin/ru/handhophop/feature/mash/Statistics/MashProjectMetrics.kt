package ru.handhophop.feature.mash.Statistics

import ru.handhophop.feature.mash.MashCreate.MashThread
import ru.handhophop.feature.mash.MashUiState
import kotlin.math.roundToInt

internal data class MashPaletteUsage(
    val paletteIndex: Int,
    val thread: MashThread,
    val cells: Int,
    val completedCells: Int,
) {
    val isCompleted: Boolean
        get() = cells > 0 && completedCells >= cells
}

internal data class MashProjectMetrics(
    val totalCells: Int = 0,
    val completedCells: Int = 0,
    val totalUsedColors: Int = 0,
    val completedUsedColors: Int = 0,
    val paletteUsage: List<MashPaletteUsage> = emptyList(),
) {
    val isReady: Boolean
        get() = totalCells > 0

    val isCompleted: Boolean
        get() = isReady && completedCells >= totalCells

    val progressFraction: Float
        get() = if (!isReady) 0f else completedCells.toFloat() / totalCells.toFloat()

    val progressPercent: Int
        get() = (progressFraction * 100f).roundToInt()
}

internal fun MashUiState.toProjectMetrics(): MashProjectMetrics {
    val currentScheme = scheme ?: return MashProjectMetrics()
    if (currentScheme.palette.isEmpty() || currentScheme.indices.isEmpty()) {
        return MashProjectMetrics()
    }

    val paletteProgress = currentScheme.buildPaletteProgress(completedCellIndices)

    val paletteUsage = currentScheme.palette.mapIndexedNotNull { index, thread ->
        val progress = paletteProgress[index]
        if (!progress.isUsed) {
            null
        } else {
            MashPaletteUsage(
                paletteIndex = index,
                thread = thread,
                cells = progress.totalCells,
                completedCells = progress.completedCells,
            )
        }
    }.sortedByDescending { it.cells }

    return MashProjectMetrics(
        totalCells = currentScheme.indices.size,
        completedCells = paletteUsage.sumOf(MashPaletteUsage::completedCells),
        totalUsedColors = paletteUsage.size,
        completedUsedColors = paletteUsage.count(MashPaletteUsage::isCompleted),
        paletteUsage = paletteUsage,
    )
}

internal fun buildWeeklyActivityValues(
    weekSpentTimeMillisByDay: List<Long>,
): List<Int> {
    if (weekSpentTimeMillisByDay.isEmpty()) {
        return List(7) { 0 }
    }

    val normalizedDays = weekSpentTimeMillisByDay.take(7).let { days ->
        if (days.size == 7) days else days + List(7 - days.size) { 0L }
    }
    val maxSpentTime = normalizedDays.maxOrNull() ?: 0L

    if (maxSpentTime <= 0L) {
        return List(7) { 0 }
    }

    return normalizedDays.map { spentTimeMillis ->
        if (spentTimeMillis <= 0L) {
            0
        } else {
            ((spentTimeMillis.toFloat() / maxSpentTime.toFloat()) * 100f)
                .roundToInt()
                .coerceIn(1, 100)
        }
    }
}
