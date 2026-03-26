package com.example.handhophop.feature.mash.presentation

internal interface UiAction

internal class ClickDownloadsAction : UiAction
internal class GenerateSchemeAction(
    val imageUrl: String?
) : UiAction
internal class HighlightingColorAction : UiAction
internal class ShadedColorAction : UiAction