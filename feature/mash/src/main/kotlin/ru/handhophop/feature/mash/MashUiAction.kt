package ru.handhophop.feature.mash

internal interface UiAction

internal class ClickDownloadsAction : UiAction
internal class GenerateSchemeAction(
    val imageUrl: String?
) : UiAction
internal class HighlightingColorAction : UiAction
internal class ShadedColorAction : UiAction