package ru.handhophop.feature.mash

import ru.handhophop.feature.mash.MashCreate.MashCreateConfig

internal interface UiAction

internal class ClickDownloadsAction(
    val projectTitle: String,
) : UiAction

internal class GenerateSchemeAction(
    val config: MashCreateConfig,
    val imageBytes: ByteArray? = null,
    val initialCompletedCellIndices: Set<Int> = emptySet(),
) : UiAction

internal class TogglePaletteHighlightAction(
    val paletteIndex: Int
) : UiAction

internal class ClickSchemeCellAction(
    val cellIndex: Int
) : UiAction

internal class ClearPaletteHighlightAction : UiAction

internal class TogglePaletteCompletedAction(
    val paletteIndex: Int
) : UiAction
