package ru.handhophop.feature.feed.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
data class FilterSectionState(
    val sectionId: Int,
    @param:StringRes val titleRes: Int,
    val options: List<FilterOptionState>
)

