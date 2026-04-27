package ru.handhophop.feature.feed.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
internal fun FeedGrid(
    state: FeedUiState.Success,
    onPhotoClicked: (String) -> Unit,
    onLoadMore: () -> Unit
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
        if (needLoadMore.value && !state.isLoadingMore) {
            onLoadMore()
        }
    }

    Column {
        RecommendedRow(
            state = state
        )

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            state = gridState,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            items(items = state.photos, key = { it.id }) { photo ->
                Card(
                    modifier = Modifier.fillMaxWidth()
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
}
