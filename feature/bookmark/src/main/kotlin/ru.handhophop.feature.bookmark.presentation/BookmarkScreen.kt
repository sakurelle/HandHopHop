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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
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
                    BookmarkFilterRow(
                        selectedFilter = state.selectedFilter,
                        onFilterSelected = viewModel::onFilterSelected,
                    )

                    if (state.photos.isEmpty()) {
                            BookmarkEmptyState(
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            BookmarkGrid(
                                state = state,
                                onPhotoSelected = onPhotoSelected,
                                onFavoriteClick = viewModel::onFavoriteClick,
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
    onFavoriteClick: (BookmarkPhotoItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(integerResource(R.integer.bookmark_grid_columns)),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = dimensionResource(R.dimen.bookmark_grid_horizontal_padding),
            end = dimensionResource(R.dimen.bookmark_grid_horizontal_padding),
            top = dimensionResource(R.dimen.bookmark_grid_top_padding),
            bottom = dimensionResource(R.dimen.bookmark_grid_bottom_padding),
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = dimensionResource(R.dimen.bookmark_grid_item_spacing),
    ) {
        items(items = state.photos, key = { it.id }) { photo ->
            BookmarkPhotoCard(
                photo = photo,
                onClick = { onPhotoSelected(photo.id, photo.photoUrl) },
                onFavoriteClick = { onFavoriteClick(photo) }
            )
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
            .padding(horizontal = dimensionResource(R.dimen.bookmark_empty_state_horizontal_padding)),
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
private fun BookmarkPhotoCard(
    photo: BookmarkPhotoItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            BookmarkPhoto(
                photo = photo,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(
                        dimensionResource(R.dimen.bookmark_grid_card_corner_radius)
                        )
                    ),
                onClick = onClick,
            )

            if (photo.canBookmark) {
                BookmarkButton(
                    isBookmarked = photo.isBookmarked,
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                )
            }

            if (photo.isStarted) {
                BookmarkProgressBadge(
                    progressPercentage = photo.progressPercentage,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun BookmarkProgressBadge(
    progressPercentage: Int,
    modifier: Modifier = Modifier,
) {
    val minProgress = integerResource(R.integer.bookmark_progress_min)
    val maxProgress = integerResource(R.integer.bookmark_progress_max)

    val safeProgress = remember(progressPercentage) {
        progressPercentage.coerceIn(minProgress, maxProgress)
    }

    val progressFraction = remember(safeProgress) {
        safeProgress / 100f
    }

    val title = if (safeProgress >= maxProgress) {
        stringResource(R.string.bookmark_progress_done)
    } else {
        stringResource(R.string.bookmark_progress_in_work, safeProgress)
    }

    Column(
        modifier = modifier
            .background(colorResource(R.color.bookmark_progress_badge_background))
            .padding(horizontal = dimensionResource(R.dimen.bookmark_progress_horizontal_padding), vertical = dimensionResource(R.dimen.bookmark_progress_vertical_padding)),
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.bookmark_progress_top_padding))
                .height(dimensionResource(R.dimen.bookmark_progress_height))
                .background(
                    color = colorResource(R.color.bookmark_progress_track_color),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.bookmark_progress_corner_radius)),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFraction)
                    .height(dimensionResource(R.dimen.bookmark_progress_height))
                    .background(
                        color = colorResource(R.color.bookmark_progress_indicator_color),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.bookmark_progress_corner_radius)),
                    ),
            )
        }
    }
}

@Composable
private fun BookmarkLoadingSkeleton(
    modifier: Modifier = Modifier,
) {
    val shimmerBrush = rememberShimmerBrush()
    val gridHeights = listOf(
        dimensionResource(R.dimen.bookmark_loading_skeleton_height_first),
        dimensionResource(R.dimen.bookmark_loading_skeleton_height_second),
        dimensionResource(R.dimen.bookmark_loading_skeleton_height_third),
        dimensionResource(R.dimen.bookmark_loading_skeleton_height_fourth),
        dimensionResource(R.dimen.bookmark_loading_skeleton_height_fifth),
        dimensionResource(R.dimen.bookmark_loading_skeleton_height_sixth),
        dimensionResource(R.dimen.bookmark_loading_skeleton_height_seventh),
        dimensionResource(R.dimen.bookmark_loading_skeleton_height_eighth),
    )
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(integerResource(R.integer.bookmark_grid_columns)),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.bookmark_loading_skeleton_horizontal_padding),
            vertical = dimensionResource(R.dimen.bookmark_loading_skeleton_vertical_padding),
        ),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.bookmark_grid_item_spacing)),
        verticalItemSpacing = dimensionResource(R.dimen.bookmark_grid_item_spacing),
        userScrollEnabled = false,
    ) {
        items(gridHeights) { itemHeight ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(
                        brush = shimmerBrush,
                        shape = RoundedCornerShape(
                            dimensionResource(R.dimen.bookmark_loading_skeleton_corner_radius),
                        ),
                    ),
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
            .padding(horizontal = dimensionResource(R.dimen.bookmark_error_state_horizontal_padding)),
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
private fun BookmarkButton(
    isBookmarked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconRes = if (isBookmarked) {
        R.drawable.ic_bookmark_filled
    } else {
        R.drawable.ic_bookmark_outline
    }

    val contentDescription = if (isBookmarked) {
        stringResource(R.string.bookmark_remove_from_favorites)
    } else {
        stringResource(R.string.bookmark_add_to_favorites)
    }

    Box(
        modifier = modifier
            .size(
                width = dimensionResource(R.dimen.bookmark_button_width),
                height = dimensionResource(R.dimen.bookmark_button_height),
            )
            .background(
                color = colorResource(R.color.bookmark_button_background),
                shape = RoundedCornerShape(
                    topStart = dimensionResource(R.dimen.bookmark_button_top_start_radius),
                    topEnd = dimensionResource(R.dimen.bookmark_button_top_end_radius),
                    bottomStart = dimensionResource(R.dimen.bookmark_button_bottom_start_radius),
                    bottomEnd = dimensionResource(R.dimen.bookmark_button_bottom_end_radius),
                ),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(
                dimensionResource(R.dimen.bookmark_heart_icon_size)
            ),
            tint = if (isBookmarked) {
                colorResource(R.color.bookmark_button_selected_icon_color)
            } else {
                colorResource(R.color.bookmark_button_unselected_icon_color)
            },
        )
    }
}

@Composable
private fun rememberShimmerBrush(): Brush {
    val colors = HandHopHopDesignSystem.colors
    val baseColor = colors.shimmerBase
    val highlightColor = colors.shimmerHighlight

    val alphaDenominator = integerResource(R.integer.bookmark_alpha_denominator)
    val baseAlpha = integerResource(R.integer.bookmark_shimmer_base_alpha_percent) /
            alphaDenominator.toFloat()
    val initialTranslateX = integerResource(R.integer.bookmark_shimmer_initial_translate_x).toFloat()
    val targetTranslateX = integerResource(R.integer.bookmark_shimmer_target_translate_x).toFloat()
    val shimmerDurationMillis = integerResource(R.integer.bookmark_shimmer_duration_millis)
    val gradientOffset = integerResource(R.integer.bookmark_shimmer_gradient_offset).toFloat()
    val gradientStartY = integerResource(R.integer.bookmark_shimmer_gradient_start_y).toFloat()
    val gradientEndY = integerResource(R.integer.bookmark_shimmer_gradient_end_y).toFloat()


    val transition = rememberInfiniteTransition(label = stringResource(R.string.bookmark_shimmer_label))
    val translateX by transition.animateFloat(
        initialValue = initialTranslateX,
        targetValue = targetTranslateX,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = shimmerDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = stringResource(R.string.bookmark_shimmer_translate_label),
    )

    return Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = baseAlpha),
            highlightColor,
            baseColor.copy(alpha = baseAlpha),
        ),
        start = Offset(translateX - gradientOffset, gradientStartY),
        end = Offset(translateX, gradientEndY),
    )
}

