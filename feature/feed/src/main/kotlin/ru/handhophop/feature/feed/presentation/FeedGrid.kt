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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.handhophop.feature.feed.R
import ru.handhophop.design.R as DesignR

@Composable
internal fun FeedGrid(
    modifier: Modifier = Modifier,
    state: FeedUiState.Success,
    onPhotoClicked: (String) -> Unit,
    onLoadMore: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val gridState = rememberLazyStaggeredGridState()

    val needLoadMore = remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = gridState.layoutInfo.totalItemsCount
            lastVisible >= total - 3
        }
    }

    LaunchedEffect(needLoadMore.value, state.isLoadingMore) {
        if (needLoadMore.value && !state.isLoadingMore && !state.isRefreshing) {
            onLoadMore()
        }
    }

    val spacing = dimensionResource(DesignR.dimen.feed_spacing)

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        state = gridState,
        contentPadding = contentPadding,
        verticalItemSpacing = spacing,
        modifier = modifier
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            RecommendedRow(
                state = state,
                onPhotoClicked = onPhotoClicked
            )
        }

        itemsIndexed(items = state.photos, key = { _, it -> it.id}) { index, photo ->
            val startPadding = if (index % 2 == 0) spacing else spacing/2
            val endPadding = if (index % 2 == 0) spacing/2 else spacing
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 8.dp,
                        end = 8.dp
                    )
            ) {
                AsyncImage(
                    model = photo.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPhotoClicked(photo.photoUrl) }
                )
            }
        }
    }

    if (state.isLoadingMore) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
