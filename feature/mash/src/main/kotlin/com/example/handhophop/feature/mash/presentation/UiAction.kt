package com.example.handhophop.feature.mash.presentation

internal sealed interface UiAction

internal data object ClickDownloadsAction : UiAction

internal data class GenerateShemaAction(
    val imageUrl: String?
) : UiAction