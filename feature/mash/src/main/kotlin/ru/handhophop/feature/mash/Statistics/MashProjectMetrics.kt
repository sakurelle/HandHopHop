package ru.handhophop.feature.mash.Statistics

import kotlin.math.roundToInt
import ru.handhophop.feature.mash.MashCreate.MashCreateDifficulty
import ru.handhophop.feature.mash.MashCreate.MashThread
import ru.handhophop.feature.mash.MashUiState

internal data class MashPaletteUsage(
    val paletteIndex: Int,
    val thread: MashThread,
    val cells: Int,
    val isCompleted: Boolean,
)

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

    val usage = IntArray(currentScheme.palette.size)
    currentScheme.indices.forEach { paletteIndex ->
        if (paletteIndex in usage.indices) {
            usage[paletteIndex]++
        }
    }

    val paletteUsage = currentScheme.palette.mapIndexedNotNull { index, thread ->
        val cells = usage[index]
        if (cells == 0) {
            null
        } else {
            MashPaletteUsage(
                paletteIndex = index,
                thread = thread,
                cells = cells,
                isCompleted = index in completedPaletteIndices,
            )
        }
    }.sortedByDescending { it.cells }

    val completedCells = paletteUsage
        .filter(MashPaletteUsage::isCompleted)
        .sumOf(MashPaletteUsage::cells)

    return MashProjectMetrics(
        totalCells = currentScheme.indices.size,
        completedCells = completedCells,
        totalUsedColors = paletteUsage.size,
        completedUsedColors = paletteUsage.count(MashPaletteUsage::isCompleted),
        paletteUsage = paletteUsage,
    )
}

internal fun MashProjectMetrics.buildWeeklyActivity(
    difficulty: MashCreateDifficulty,
): List<Int> {
    if (!isReady) {
        return List(7) { 0 }
    }

    val difficultyMultiplier = when (difficulty) {
        MashCreateDifficulty.EASY -> 1.7f
        MashCreateDifficulty.MEDIUM -> 2.2f
        MashCreateDifficulty.HARD -> 2.8f
    }

    val pattern = listOf(0.56f, 0.7f, 0.64f, 0.82f, 1f, 0.9f, 0.74f)

    return pattern.mapIndexed { index, weight ->
        val colorBoost = if (index < completedUsedColors) 0.18f else 0f
        ((progressFraction * difficultyMultiplier + colorBoost) * 4f * weight)
            .roundToInt()
            .coerceIn(0, 4)
    }
}
