package ru.handhophop.feature.feed.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.feature.feed.R
import ru.handhophop.design.R as DesignR

@Composable
internal fun FeedGrid(
    modifier: Modifier = Modifier,
    state: FeedUiState.Success,
    onPhotoClicked: (String) -> Unit,
    onFavoriteClick: (FeedPhotoItem) -> Unit,
    onLoadMore: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val colors = HandHopHopDesignSystem.colors
    val gridState = rememberLazyStaggeredGridState()
    val loadMoreThreshold = integerResource(R.integer.feed_load_more_threshold)

    val needLoadMore = remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = gridState.layoutInfo.totalItemsCount
            lastVisible >= total - loadMoreThreshold
        }
    }

    LaunchedEffect(needLoadMore.value, state.isLoadingMore) {
        if (needLoadMore.value && !state.isLoadingMore && !state.isRefreshing) {
            onLoadMore()
        }
    }

    val spacing = dimensionResource(DesignR.dimen.feed_spacing)

    LazyVerticalStaggeredGrid(
        modifier = modifier,
        columns = StaggeredGridCells.Fixed(integerResource(R.integer.feed_grid_columns)),
        state = gridState,
        contentPadding = contentPadding,
        verticalItemSpacing = spacing,
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            RecommendedRow(
                state = state,
                onPhotoClicked = onPhotoClicked,
                onFavoriteClick = onFavoriteClick
            )
        }

        itemsIndexed(items = state.photos, key = { _, it -> it.id }) { _, photo ->
            FeedPhotoCard(
                photo = photo,
                onClick = { onPhotoClicked(photo.photoUrl) },
                onFavoriteClick = { onFavoriteClick(photo) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(R.dimen.feed_grid_card_horizontal_padding),
                    ),
            )
        }

        if (state.isLoadingMore) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.feed_grid_loading_more_padding)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = colors.primaryAction,
                    )
                }
            }
        }
    }
}
@Composable
private fun FeedPhotoCard(
    photo: FeedPhotoItem,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = photo.photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(
                        dimensionResource(R.dimen.feed_grid_card_corner_radius),
                        ),
                    )
                    .clickable(onClick = onClick)
            )

            if (photo.isStarted) {
                FeedProgressCircle(
                    progressPercentage = photo.progressPercentage,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(dimensionResource(R.dimen.feed_grid_progress_padding)),
                )
            }

            FeedBookmarkButton(
                isBookmarked = photo.isBookmarked,
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
            )
        }
    }
}

