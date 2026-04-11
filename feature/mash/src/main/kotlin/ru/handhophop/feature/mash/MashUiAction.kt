package ru.handhophop.feature.mash

import ru.handhophop.feature.mash.MashCreate.MashCreateConfig

internal interface UiAction

internal class ClickDownloadsAction : UiAction

internal class GenerateSchemeAction(
    val config: MashCreateConfig
) : UiAction

internal class TogglePaletteHighlightAction(
    val paletteIndex: Int
) : UiAction

internal class HighlightingColorAction : UiAction

internal class ShadedColorAction : UiAction