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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.handhophop.core.design.ExposableTopBar
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.core.design.HandHopHopDesignTheme
import ru.handhophop.core.design.TopBarState
import ru.handhophop.feature.feed.R
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
    val successState = uiState as? FeedUiState.Success

    val spacing = dimensionResource(DesignR.dimen.feed_spacing)
    val topPadding = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding() + dimensionResource(DesignR.dimen.top_spacing)

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
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
                        onFavoriteClick = {photo ->
                            viewModel.handleAction(FeedUiAction.FavoriteClicked(photo))
                        },
                        onLoadMore = { viewModel.handleAction(FeedUiAction.LoadNextPage) },
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
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.feed_error_message, message),
            color = colors.error,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun FeedLoadingSkeleton(
    modifier: Modifier = Modifier,
) {
    HandHopHopDesignSystem.colors
    val shimmerBrush = rememberShimmerBrush()
    val recommendedPlaceholders = remember { List(4) { it } }
    val firstHeight = dimensionResource(DesignR.dimen.feed_loading_grid_height_first)
    val secondHeight = dimensionResource(DesignR.dimen.feed_loading_grid_height_second)
    val thirdHeight = dimensionResource(DesignR.dimen.feed_loading_grid_height_third)
    val fourthHeight = dimensionResource(DesignR.dimen.feed_loading_grid_height_fourth)
    val fifthHeight = dimensionResource(DesignR.dimen.feed_loading_grid_height_fifth)
    val sixthHeight = dimensionResource(DesignR.dimen.feed_loading_grid_height_sixth)
    val seventhHeight = dimensionResource(DesignR.dimen.feed_loading_grid_height_seventh)
    val eighthHeight = dimensionResource(DesignR.dimen.feed_loading_grid_height_eighth)
    val gridHeights = remember(
        firstHeight,
        secondHeight,
        thirdHeight,
        fourthHeight,
        fifthHeight,
        sixthHeight,
        seventhHeight,
        eighthHeight,
    ) {
        listOf(
            firstHeight,
            secondHeight,
            thirdHeight,
            fourthHeight,
            fifthHeight,
            sixthHeight,
            seventhHeight,
            eighthHeight,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(DesignR.dimen.feed_loading_skeleton_vertical_padding)),
        ) {
            Box(
                modifier = Modifier
                    .padding(
                        horizontal = dimensionResource(DesignR.dimen.feed_loading_header_horizontal_padding),
                        vertical = dimensionResource(DesignR.dimen.feed_loading_header_vertical_padding),
                    )
                    .width(dimensionResource(DesignR.dimen.feed_loading_header_width))
                    .height(dimensionResource(DesignR.dimen.feed_loading_header_height))
                    .clip(
                        RoundedCornerShape(
                            dimensionResource(DesignR.dimen.feed_loading_skeleton_corner_radius),
                        ),
                    )
                    .background(shimmerBrush),
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = dimensionResource(DesignR.dimen.feed_loading_recommended_horizontal_padding)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesignR.dimen.feed_loading_recommended_item_spacing)),
            ) {
                items(recommendedPlaceholders.size) {
                    Box(
                        modifier = Modifier
                            .size(dimensionResource(DesignR.dimen.feed_loading_recommended_item_size))
                            .clip(RoundedCornerShape(dimensionResource(DesignR.dimen.feed_loading_skeleton_corner_radius)))
                            .background(shimmerBrush),
                    )
                }
            }
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(integerResource(R.integer.feed_loading_grid_columns)),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                horizontal = dimensionResource(DesignR.dimen.feed_loading_grid_horizontal_padding),
                vertical = dimensionResource(DesignR.dimen.feed_loading_grid_vertical_padding),
            ),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesignR.dimen.feed_loading_grid_item_spacing)),
            verticalItemSpacing = dimensionResource(DesignR.dimen.feed_loading_grid_item_spacing),
            userScrollEnabled = false,
        ) {
            items(gridHeights) { itemHeight ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clip(RoundedCornerShape(dimensionResource(DesignR.dimen.feed_loading_grid_card_corner_radius)))
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

    val shimmerAlpha = integerResource(R.integer.feed_shimmer_base_alpha_percent).toFloat() /
            integerResource(R.integer.feed_alpha_denominator).toFloat()
    val initialTranslateX = integerResource(R.integer.feed_shimmer_initial_translate_x).toFloat()
    val targetTranslateX = integerResource(R.integer.feed_shimmer_target_translate_x).toFloat()
    val durationMillis = integerResource(R.integer.feed_shimmer_duration_millis)
    val gradientOffset = integerResource(R.integer.feed_shimmer_gradient_offset).toFloat()
    val gradientStartY = integerResource(R.integer.feed_shimmer_gradient_start_y).toFloat()
    val gradientEndY = integerResource(R.integer.feed_shimmer_gradient_end_y).toFloat()


    val transition = rememberInfiniteTransition(label = "feed_shimmer")
    val translateX by transition.animateFloat(
        initialValue = initialTranslateX,
        targetValue = targetTranslateX,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "feed_shimmer_translate"
    )

    return Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = shimmerAlpha),
            highlightColor,
            baseColor.copy(alpha = shimmerAlpha)
        ),
        start = Offset(translateX - gradientOffset, gradientStartY),
        end = Offset(translateX, gradientEndY)
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
