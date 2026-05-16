package ru.handhophop.feature.feed.presentation

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.handhophop.core.design.BackgroundPattern
import ru.handhophop.core.design.ExposableTopBar
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.core.design.HandHopHopDesignTheme
import ru.handhophop.core.design.TopBarState
import ru.handhophop.design.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeedScreen(
    viewModel: FeedViewModel,
    onPhotoSelected: (String) -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.handleAction(FeedUiAction.LoadPhotos)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = HandHopHopDesignSystem.colors
    val successState = uiState as? FeedUiState.Success

    val spacing = dimensionResource(DesignR.dimen.feed_spacing)
    val topPadding = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding() + dimensionResource(DesignR.dimen.top_spacing)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        BackgroundPattern()

        when (val state = uiState) {
            is FeedUiState.Loading -> {
                FeedLoadingSkeleton(
                    modifier = Modifier.padding(
                        top = topPadding,
                        bottom = spacing,
                    ),
                )
            }

            is FeedUiState.Error -> {
                FeedErrorState(
                    message = state.msg,
                    modifier = Modifier.padding(top = topPadding),
                )
            }

            is FeedUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = {
                        viewModel.handleAction(FeedUiAction.Refresh)
                    },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    FeedGrid(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        onPhotoClicked = onPhotoSelected,
                        onLoadMore = {
                            viewModel.handleAction(FeedUiAction.LoadNextPage)
                        },
                        contentPadding = PaddingValues(
                            top = topPadding,
                            bottom = spacing,
                        ),
                    )
                }
            }
        }

        ExposableTopBar(
            state = TopBarState(
                titleRes = DesignR.string.feed_title,
                leftIconRes = null,
                rightIconRes = successState?.let {
                    if (it.isFilterVisible) {
                        DesignR.drawable.open_filter
                    } else {
                        DesignR.drawable.filter
                    }
                },
            ),
            onChanged = { isVisible ->
                viewModel.handleAction(FeedUiAction.SetFilterVisibility(isVisible))
            },
        ) { onDismiss ->
            successState?.let { state ->
                FeedFilterSheet(
                    sections = state.filterSections,
                    onOptionSelected = { sectionId, optionId ->
                        viewModel.handleAction(
                            FeedUiAction.ApplyFilter(sectionId, optionId),
                        )
                    },
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun FeedErrorState(
    message: String,
    modifier: Modifier = Modifier,
) {
    val colors = HandHopHopDesignSystem.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Error: $message",
            color = colors.error,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun FeedLoadingSkeleton(
    modifier: Modifier = Modifier,
) {
    val colors = HandHopHopDesignSystem.colors
    val shimmerBrush = rememberShimmerBrush()
    val recommendedPlaceholders = List(4) { it }
    val gridHeights = listOf(180.dp, 240.dp, 220.dp, 160.dp, 260.dp, 190.dp, 210.dp, 250.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .width(160.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shimmerBrush),
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(recommendedPlaceholders.size) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(shimmerBrush),
                    )
                }
            }
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            userScrollEnabled = false,
        ) {
            items(gridHeights) { itemHeight ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clip(RoundedCornerShape(12.dp))
                        .background(shimmerBrush),
                )
            }
        }
    }
}

@Composable
private fun rememberShimmerBrush(): Brush {
    val colors = HandHopHopDesignSystem.colors
    val baseColor = colors.shimmerBase
    val highlightColor = colors.shimmerHighlight

    val transition = rememberInfiniteTransition(label = "feed_shimmer")
    val translateX by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "feed_shimmer_translate",
    )

    return Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.9f),
            highlightColor,
            baseColor.copy(alpha = 0.9f),
        ),
        start = Offset(translateX - 300f, 0f),
        end = Offset(translateX, 300f),
    )
}

@Preview(name = "Feed Loading Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Feed Loading Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FeedLoadingSkeletonPreview() {
    HandHopHopDesignTheme {
        FeedLoadingSkeleton()
    }
}

@Preview(name = "Feed Error Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Feed Error Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FeedErrorStatePreview() {
    HandHopHopDesignTheme {
        FeedErrorState(message = "Network unavailable")
    }
}