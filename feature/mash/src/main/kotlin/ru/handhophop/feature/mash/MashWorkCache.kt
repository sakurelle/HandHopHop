package ru.handhophop.feature.mash

import ru.handhophop.feature.mash.MashCreate.MashCreateConfig

internal data class CachedMashWork(
    val config: MashCreateConfig,
    val uiState: MashUiState,
)

internal object MashWorkCache {
    var currentWork: CachedMashWork? = null
}
