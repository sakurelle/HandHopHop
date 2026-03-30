package ru.handhophop.feature.mash

import ru.handhophop.feature.mash.complexity.ComplexityType

internal interface UiAction

internal class ClickDownloadsAction : UiAction

internal class GenerateSchemeAction(
    val imageUrl: String?
) : UiAction

internal class HighlightingColorAction : UiAction

internal class ShadedColorAction : UiAction

internal class SelectComplexityAction(
    val complexity: ComplexityType
) : UiAction