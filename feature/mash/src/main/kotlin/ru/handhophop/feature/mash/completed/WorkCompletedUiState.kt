package ru.handhophop.feature.mash.completed

import androidx.compose.runtime.Immutable

@Immutable
internal data class WorkCompletedUiState(
    val isLoading: Boolean = false,
    val recommendations: List<WorkCompletedRecommendationItem> = emptyList(),
)

@Immutable
internal data class WorkCompletedRecommendationItem(
    val id: String,
    val photoUrl: String,
    val isBookmarked: Boolean = false,
)