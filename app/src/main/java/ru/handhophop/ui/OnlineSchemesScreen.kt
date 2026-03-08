package ru.handhophop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import ru.handhophop.data.ImageItem
import kotlin.math.abs
import ru.handhophop.R

@Composable
fun OnlineSchemesScreen(
    navController: NavHostController,
    viewModel: OnlineSchemesViewModel = viewModel(),
    onItemClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val bg = colorResource(R.color.bg_beige)

    val topBannerH = dimensionResource(R.dimen.top_banner_height)
    val bottomBarH = dimensionResource(R.dimen.bottom_bar_height)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        BackgroundPattern()

        OnlineSchemesMasonry(
            modifier = Modifier.fillMaxSize(),
            items = state.items,
            isLoading = state.isLoading,
            reachedEnd = state.reachedEnd,
            error = state.error,
            onNeedMore = { viewModel.loadMore() },
            onClickItem = { item -> onItemClick(item.imageUrl) },
            topInset = topBannerH,
            bottomInset = bottomBarH
        )

        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            TopBanner(title = "Онлайн схемы")
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomBar(navController = navController, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun OnlineSchemesMasonry(
    modifier: Modifier = Modifier,
    items: List<ImageItem>,
    isLoading: Boolean,
    reachedEnd: Boolean,
    error: String?,
    onNeedMore: () -> Unit,
    onClickItem: (ImageItem) -> Unit,
    topInset: Dp,
    bottomInset: Dp
) {
    val gridState = rememberLazyStaggeredGridState()
    val ratioCache = remember { mutableStateMapOf<String, Float>() }

    val shouldLoadMore by remember {
        derivedStateOf {
            if (items.isEmpty()) return@derivedStateOf false
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= items.size - 6
        }
    }

    LaunchedEffect(shouldLoadMore, isLoading, reachedEnd) {
        if (shouldLoadMore && !isLoading && !reachedEnd) onNeedMore()
    }

    Box(modifier = modifier) {

        LazyVerticalStaggeredGrid(
            state = gridState,
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = topInset + 8.dp,
                bottom = bottomInset + 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            items(items, key = { it.id }) { item ->
                val url = item.imageUrl
                var ratio by remember(url) { mutableStateOf(ratioCache[url] ?: 1f) }

                Card(
                    onClick = { onClickItem(item) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    SubcomposeAsyncImage(
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(ratio)
                    ) {
                        val st = painter.state
                        if (st is AsyncImagePainter.State.Success) {
                            val s = painter.intrinsicSize
                            if (s.width > 0f && s.height > 0f) {
                                val newRatio = s.width / s.height
                                if (abs(newRatio - ratio) > 0.02f) {
                                    ratio = newRatio
                                    ratioCache[url] = newRatio
                                }
                            }
                        }
                        SubcomposeAsyncImageContent()
                    }
                }
            }
        }

        if (items.isEmpty() && !isLoading && error == null) {
            Text(
                text = "Нет изображений",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomInset + 24.dp)
            )
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        }
    }
}