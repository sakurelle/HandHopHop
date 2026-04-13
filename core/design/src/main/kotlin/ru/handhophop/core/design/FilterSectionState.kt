package ru.handhophop.core.design

import androidx.compose.runtime.Immutable

@Immutable
data class FilterSectionState(
    val title: String,
    val options: List<String>,
    val id: List<String>,
    val selectedOptionId: String
)

