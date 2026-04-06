package ru.handhophop.core.design

import androidx.compose.runtime.Immutable

@Immutable
open class TopBarState(
    val topBarTitle: String
)

@Immutable
data class FilterTopBarState(
    val isFilterActive: Boolean,
    //TODO тут должны быть все нужные поля для стейта топ бара с фильтрацией
    val title: String
): TopBarState(title)