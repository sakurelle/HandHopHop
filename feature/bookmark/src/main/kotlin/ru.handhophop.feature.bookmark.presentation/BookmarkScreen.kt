package ru.handhophop.feature.bookmark.presentation

import java.io.File
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
            appContext = context.applicationContext,
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
    val colors = HandHopHopDesignSystem.colors

    Scaffold(
        topBar = {
            TopBar(
                state = TopBarState(
                    titleRes = R.string.bookmark_highlight_title,
                ),
                { },
                { },
            )
        },
        containerColor = Color.Transparent,
        contentColor = colors.textPrimary,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (val state = uiState) {
                BookmarkUiState.Loading -> {
                    BookmarkLoadingSkeleton(
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is BookmarkUiState.Error -> {
                    BookmarkErrorState(
                        message = state.message,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is BookmarkUiState.Success -> {
                    if (state.photos.isEmpty()) {
                        BookmarkEmptyState(
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        BookmarkGrid(
                            state = state,
                            onPhotoSelected = onPhotoSelected,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
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
    val colors = HandHopHopDesignSystem.colors
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
    ) {
        items(items = state.photos, key = { it.id }) { photo ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface,
                ),
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
    val colors = HandHopHopDesignSystem.colors
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
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.bookmark_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
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
    val model = remember(photo.imagePath, photo.photoUrl) {
        photo.imagePath
            ?.takeIf { it.isNotBlank() }
            ?.let(::File)
            ?.takeIf(File::exists)
            ?: photo.photoUrl
    }

    AsyncImage(
        model = model,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.clickable(onClick = onClick),
    )
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
    val colors = HandHopHopDesignSystem.colors
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
                color = colors.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun rememberShimmerBrush(): Brush {
    val colors = HandHopHopDesignSystem.colors
    val baseColor = colors.shimmerBase
    val highlightColor = colors.shimmerHighlight
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

//@Preview(name = "Bookmark Empty Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
//@Preview(name = "Bookmark Empty Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
//@Composable
//private fun BookmarkEmptyStatePreview() {
//    HandHopHopDesignTheme {
//        BookmarkEmptyState()
//    }
//}
//
//@Preview(name = "Bookmark Error Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
//@Preview(name = "Bookmark Error Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
//@Composable
//private fun BookmarkErrorStatePreview() {
//    HandHopHopDesignTheme {
//        BookmarkErrorState(message = "Failed to load favorites")
//    }
//}
