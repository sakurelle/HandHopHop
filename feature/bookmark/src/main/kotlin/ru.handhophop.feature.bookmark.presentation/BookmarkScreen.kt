package ru.handhophop.feature.bookmark.presentation

import android.graphics.BitmapFactory
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.core.design.TopBar
import ru.handhophop.core.design.TopBarState
import ru.handhophop.core.system.database.HandHopHopDatabaseProvider
import ru.handhophop.core.system.database.work.WorkLocalRepository
import ru.handhophop.feature.bookmark.R

@Composable
fun BookmarkEntryPoint(
    onPhotoSelected: (Long, String) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val repository = remember(context) {
        WorkLocalRepository(
            workDao = HandHopHopDatabaseProvider.get(context).workDao(),
        )
    }
    val viewModel: BookmarkViewModel = viewModel(
        factory = BookmarkViewModel.Factory(repository),
    )

    LaunchedEffect(viewModel) {
        viewModel.loadBookmarks()
    }

    BookmarkScreen(
        viewModel = viewModel,
        onPhotoSelected = onPhotoSelected,
    )
}

@Composable
private fun BookmarkScreen(
    viewModel: BookmarkViewModel,
    onPhotoSelected: (Long, String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        TopBar(
            state = TopBarState(
                titleRes = R.string.bookmark_highlight_title
            ),
            {Unit},
            {Unit}
        )

        when (val state = uiState) {
            BookmarkUiState.Loading -> BookmarkLoadingSkeleton(
                modifier = Modifier.weight(1f),
            )
            is BookmarkUiState.Error -> BookmarkErrorState(
                message = state.message,
                modifier = Modifier.weight(1f),
            )
            is BookmarkUiState.Success -> {
                if (state.photos.isEmpty()) {
                    BookmarkEmptyState(modifier = Modifier.weight(1f))
                } else {
                    BookmarkGrid(
                        state = state,
                        onPhotoSelected = onPhotoSelected,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}


@Composable
private fun BookmarkGrid(
    state: BookmarkUiState.Success,
    onPhotoSelected: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
    ) {
        items(items = state.photos, key = { it.id }) { photo ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                BookmarkPhoto(
                    photo = photo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    onClick = { onPhotoSelected(photo.id, photo.photoUrl) },
                )
            }
        }
    }
}

@Composable
private fun BookmarkEmptyState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.bookmark_empty_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.bookmark_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BookmarkPhoto(
    photo: BookmarkPhotoItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val bitmap = remember(photo.imageBytes) {
        photo.imageBytes?.let { imageBytes ->
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }

    if (bitmap == null) {
        AsyncImage(
            model = photo.photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.clickable(onClick = onClick),
        )
    } else {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.clickable(onClick = onClick),
        )
    }
}

@Composable
private fun BookmarkLoadingSkeleton(
    modifier: Modifier = Modifier,
) {
    val shimmerBrush = rememberShimmerBrush()
    val gridHeights = listOf(180.dp, 240.dp, 220.dp, 160.dp, 260.dp, 190.dp, 210.dp, 250.dp)

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
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

@Composable
private fun BookmarkErrorState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.bookmark_error_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun rememberShimmerBrush(): Brush {
    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = Color.White.copy(alpha = 0.6f)
    val transition = rememberInfiniteTransition(label = "bookmark_shimmer")
    val translateX by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "bookmark_shimmer_translate",
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
