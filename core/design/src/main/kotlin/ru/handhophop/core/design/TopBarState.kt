package ru.handhophop.core.design

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
data class TopBarState(
    @param:StringRes val titleRes: Int?,
    @param:DrawableRes val leftIconRes: Int? =null,
    @param:DrawableRes val rightIconRes: Int?=null,
    val projectName: String? = null,
)
