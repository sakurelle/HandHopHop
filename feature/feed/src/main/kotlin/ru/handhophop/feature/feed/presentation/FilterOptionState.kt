package ru.handhophop.feature.feed.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
data class FilterOptionState(
    @param:StringRes val textRes: Int,
    val id: Int,
    val isSelected: Boolean,
)
