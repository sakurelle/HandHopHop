package ru.handhophop.feature.mash

import ru.handhophop.core.system.database.work.WorkLocalItem
import ru.handhophop.feature.mash.MashCreate.MashCreateConfig
import ru.handhophop.feature.mash.MashCreate.MashCreateDifficulty
import ru.handhophop.feature.mash.MashCreate.MashCreateSchemeType

internal fun MashCreateConfig.toWorkLocalItem(
    id: Long = 0,
    image: ByteArray? = null,
    isFavorite: Boolean = false,
    uiState: MashUiState? = null,
): WorkLocalItem {
    val scheme = uiState?.scheme
    val completedCells = uiState?.completedCellIndices.orEmpty()

    return WorkLocalItem(
        id = id,
        url = imageUrl.orEmpty(),
        image = image,
        isFavorite = isFavorite,
        projectName = projectName,
        schemeType = schemeType.name,
        colorCount = colorCount,
        difficulty = difficulty.name,
        gridWidth = scheme?.gridW,
        gridHeight = scheme?.gridH,
        gridRle = encodeCompletedCellsRle(
            totalCells = scheme?.indices?.size ?: 0,
            completedCells = completedCells,
        ),
        percentage = buildPercentage(
            totalCells = scheme?.indices?.size ?: 0,
            completedCells = completedCells.size,
        ),
    )
}

internal fun WorkLocalItem.toMashCreateConfigOrNull(): MashCreateConfig? {
    val difficulty = difficulty
        ?.let(::difficultyValueOfOrNull)
        ?: return null
    val schemeType = schemeType
        ?.let(::schemeTypeValueOfOrNull)
        ?: return null
    val projectName = projectName ?: return null

    return MashCreateConfig(
        projectName = projectName,
        imageUrl = url,
        schemeType = schemeType,
        colorCount = colorCount ?: return null,
        difficulty = difficulty,
        threads = emptyList(),
    )
}

internal fun WorkLocalItem.decodeCompletedCells(): Set<Int> {
    return decodeCompletedCellsRle(
        totalCells = (gridWidth ?: 0) * (gridHeight ?: 0),
        rle = gridRle,
    )
}

private fun buildPercentage(
    totalCells: Int,
    completedCells: Int,
): Int? {
    if (totalCells <= 0) return null
    return (completedCells * 100) / totalCells
}

private fun encodeCompletedCellsRle(
    totalCells: Int,
    completedCells: Set<Int>,
): String? {
    if (totalCells <= 0) return null

    val states = BooleanArray(totalCells)
    completedCells.forEach { index ->
        if (index in 0 until totalCells) {
            states[index] = true
        }
    }

    val result = StringBuilder()
    var currentValue = if (states.firstOrNull() == true) 1 else 0
    var currentCount = 0

    states.forEach { isCompleted ->
        val value = if (isCompleted) 1 else 0
        if (value == currentValue) {
            currentCount++
        } else {
            appendRun(result, currentValue, currentCount)
            currentValue = value
            currentCount = 1
        }
    }
    appendRun(result, currentValue, currentCount)

    return result.toString()
}

private fun appendRun(
    builder: StringBuilder,
    value: Int,
    count: Int,
) {
    if (count <= 0) return
    if (builder.isNotEmpty()) {
        builder.append(';')
    }
    builder.append(value)
        .append(':')
        .append(count)
}

private fun decodeCompletedCellsRle(
    totalCells: Int,
    rle: String?,
): Set<Int> {
    if (totalCells <= 0 || rle.isNullOrBlank()) return emptySet()

    val completed = linkedSetOf<Int>()
    var currentIndex = 0

    rle.split(';').forEach { chunk ->
        val parts = chunk.split(':')
        if (parts.size != 2) return@forEach

        val value = parts[0].toIntOrNull() ?: return@forEach
        val count = parts[1].toIntOrNull() ?: return@forEach

        repeat(count) {
            if (currentIndex >= totalCells) return@repeat
            if (value == 1) {
                completed += currentIndex
            }
            currentIndex++
        }
    }

    return completed
}

private fun difficultyValueOfOrNull(name: String): MashCreateDifficulty? {
    return MashCreateDifficulty.entries.firstOrNull { it.name == name }
}

private fun schemeTypeValueOfOrNull(name: String): MashCreateSchemeType? {
    return MashCreateSchemeType.entries.firstOrNull { it.name == name }
}
