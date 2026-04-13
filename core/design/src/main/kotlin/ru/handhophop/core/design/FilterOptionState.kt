package ru.handhophop.core.design

import androidx.compose.runtime.Immutable

@Immutable
data class FilterOptionState(
    val text: String,
    val id: String,
    val isSelected: Boolean,
)
