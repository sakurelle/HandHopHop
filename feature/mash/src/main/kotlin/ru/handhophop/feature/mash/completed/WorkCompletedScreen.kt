package ru.handhophop.feature.mash.completed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.core.design.TopBar
import ru.handhophop.core.design.TopBarState
import ru.handhophop.core.network.WallhavenNetwork
import ru.handhophop.core.system.database.HandHopHopDatabaseProvider
import ru.handhophop.core.system.database.work.WorkLocalRepository
import ru.handhophop.feature.mash.R
import java.io.File
import ru.handhophop.design.R as DesignR

@Composable
internal fun WorkCompletedEntryPoint(
    imageUrl: String?,
    imagePath: String?,
    projectTitle: String,
    onBackClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onOpenWorkClick: () -> Unit,
    onRecommendationClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val workLocalRepository = androidx.compose.runtime.remember(context) {
        WorkLocalRepository(
            workDao = HandHopHopDatabaseProvider.get(context).workDao(),
            appContext = context.applicationContext,
        )
    }

    val viewModel: WorkCompletedViewModel = viewModel(
        factory = WorkCompletedViewModel.Factory(
            apiService = WallhavenNetwork.getApiService(),
            workLocalRepository = workLocalRepository,
        ),
    )

    LaunchedEffect(viewModel) {
        viewModel.loadRecommendations()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WorkCompletedScreen(
        imageUrl = imageUrl,
        imagePath = imagePath,
        projectTitle = projectTitle,
        uiState = uiState,
        onBackClick = onBackClick,
        onDownloadClick = onDownloadClick,
        onOpenWorkClick = onOpenWorkClick,
        onRecommendationClick = onRecommendationClick,
        onRecommendationFavoriteClick = viewModel::onFavoriteClick,
        onSeeAllClick = onSeeAllClick,
    )
}

@Composable
private fun WorkCompletedScreen(
    imageUrl: String?,
    imagePath: String?,
    projectTitle: String,
    uiState: WorkCompletedUiState,
    onBackClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onOpenWorkClick: () -> Unit,
    onRecommendationClick: (String) -> Unit,
    onRecommendationFavoriteClick: (WorkCompletedRecommendationItem) -> Unit,
    onSeeAllClick: () -> Unit,
) {
    val colors = HandHopHopDesignSystem.colors

    Scaffold(
        topBar = {
            TopBar(
                state = TopBarState(
                    titleRes = R.string.work_completed_title,
                    leftIconRes = DesignR.drawable.arrow,
                ),
                onClickRight = { Unit },
                onClickLeft = onBackClick,
            )
        },
        containerColor = Color.Transparent,
        contentColor = colors.textPrimary,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()+dimensionResource(R.dimen.work_completed_top_spacing))
                .padding(horizontal = dimensionResource(R.dimen.work_completed_screen_horizontal_padding))
                .padding(top = dimensionResource(R.dimen.work_completed_top_spacing)),
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.work_completed_content_spacing),
            ),
        ) {
            WorkCompletedHeroCard(
                imageUrl = imageUrl,
                imagePath = imagePath,
                onDownloadClick = onDownloadClick,
                onOpenWorkClick = onOpenWorkClick,
            )

            WorkCompletedRecommendationsHeader(
                onSeeAllClick = onSeeAllClick,
            )

            WorkCompletedRecommendationsGrid(
                uiState = uiState,
                onRecommendationClick = onRecommendationClick,
                onFavoriteClick = onRecommendationFavoriteClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun WorkCompletedHeroCard(
    imageUrl: String?,
    imagePath: String?,
    onDownloadClick: () -> Unit,
    onOpenWorkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HandHopHopDesignSystem.colors
    val imageModel = imagePath
        ?.let(::File)
        ?.takeIf(File::exists)
        ?: imageUrl

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.work_completed_card_radius)),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface,
        ),
        border = BorderStroke(
            width = dimensionResource(R.dimen.work_completed_hero_border_width),
            color = colorResource(R.color.work_completed_hero_border_color),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(R.dimen.work_completed_card_elevation),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.work_completed_card_padding)),
            horizontalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.work_completed_card_inner_spacing),
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .aspectRatio(0.75f),
            ) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(dimensionResource(R.dimen.work_completed_image_radius))),
                    contentScale = ContentScale.Crop,
                )

                WorkCompletedCheckBadge(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = -dimensionResource(R.dimen.work_completed_check_offset),
                            y = -dimensionResource(R.dimen.work_completed_check_offset),
                        ),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.work_completed_text_spacing),
                ),
            ) {
                Text(
                    text = stringResource(R.string.work_completed_ready_title),
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                )

                Text(
                    text = stringResource(R.string.work_completed_progress_text),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                )

                Text(
                    text = stringResource(R.string.work_completed_description),
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )

                WorkCompletedPrimaryButton(
                    text = stringResource(R.string.work_completed_download_button),
                    onClick = onDownloadClick,
                )

                WorkCompletedSecondaryButton(
                    text = stringResource(R.string.work_completed_open_button),
                    onClick = onOpenWorkClick,
                )
            }
        }
    }
}

@Composable
private fun WorkCompletedCheckBadge(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(dimensionResource(R.dimen.work_completed_check_badge_size))
            .background(
                color = colorResource(R.color.work_completed_check_background),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_work_completed_check),
            contentDescription = null,
            tint = colorResource(R.color.work_completed_check_color),
            modifier = Modifier.size(
                dimensionResource(R.dimen.work_completed_check_icon_size),
            ),
        )
    }
}

@Composable
private fun WorkCompletedPrimaryButton(
    text: String,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.work_completed_button_height)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.work_completed_button_radius)),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = colorResource(DesignR.color.button),
            contentColor = colorResource(DesignR.color.white),
        ),
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun WorkCompletedSecondaryButton(
    text: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.work_completed_button_height)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.work_completed_button_radius)),
        border = BorderStroke(
            width = dimensionResource(R.dimen.work_completed_secondary_button_border_width),
            color = colorResource(R.color.work_completed_secondary_button_border_color),
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colorResource(DesignR.color.button),
        ),
    ) {
        Text(text = text)
    }
}

@Composable
private fun WorkCompletedRecommendationsHeader(
    onSeeAllClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.work_completed_recommendations_title),
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = HandHopHopDesignSystem.colors.textPrimary,
        )

        Row(
            modifier = Modifier.clickable(onClick = onSeeAllClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.work_completed_see_all),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = HandHopHopDesignSystem.colors.textSecondary,
            )

            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = colorResource(R.color.work_completed_accent_color),
                modifier = Modifier.size(
                    dimensionResource(R.dimen.work_completed_see_all_icon_size),
                ),
            )
        }
    }
}

@Composable
private fun WorkCompletedRecommendationsGrid(
    uiState: WorkCompletedUiState,
    onRecommendationClick: (String) -> Unit,
    onFavoriteClick: (WorkCompletedRecommendationItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isLoading) {
        Box(
            modifier = modifier.height(dimensionResource(R.dimen.work_completed_recommendations_grid_height)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                color = HandHopHopDesignSystem.colors.primaryAction,
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(R.integer.work_completed_recommendations_columns)),
        modifier = modifier.height(dimensionResource(R.dimen.work_completed_recommendations_grid_height)),
        contentPadding = PaddingValues(
            bottom = dimensionResource(R.dimen.work_completed_recommendations_bottom_padding),
        ),
        horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.work_completed_recommendations_spacing),
        ),
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.work_completed_recommendations_spacing),
        ),
        userScrollEnabled = false,
    ) {
        items(
            items = uiState.recommendations,
            key = WorkCompletedRecommendationItem::id,
        ) { item ->
            WorkCompletedRecommendationCard(
                item = item,
                onClick = { onRecommendationClick(item.photoUrl) },
                onFavoriteClick = { onFavoriteClick(item) },
            )
        }
    }
}

@Composable
private fun WorkCompletedRecommendationCard(
    item: WorkCompletedRecommendationItem,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(dimensionResource(R.dimen.work_completed_recommendation_card_radius)))
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = item.photoUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        WorkCompletedBookmarkButton(
            isBookmarked = item.isBookmarked,
            onClick = onFavoriteClick,
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }
}

@Composable
private fun WorkCompletedBookmarkButton(
    isBookmarked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconPainter: Painter = painterResource(
        if (isBookmarked) {
            DesignR.drawable.ic_bookmark_filled
        } else {
            DesignR.drawable.ic_bookmark_outline
        },
    )

    Box(
        modifier = modifier
            .size(
                width = dimensionResource(R.dimen.work_completed_bookmark_button_width),
                height = dimensionResource(R.dimen.work_completed_bookmark_button_height),
            )
            .background(
                color = colorResource(R.color.work_completed_bookmark_background),
                shape = RoundedCornerShape(
                    bottomStart = dimensionResource(R.dimen.work_completed_bookmark_button_bottom_start_radius),
                ),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = if (isBookmarked) {
                stringResource(R.string.work_completed_remove_from_favorites)
            } else {
                stringResource(R.string.work_completed_add_to_favorites)
            },
            tint = if (isBookmarked) {
                colorResource(R.color.work_completed_bookmark_selected_color)
            } else {
                colorResource(R.color.work_completed_bookmark_unselected_color)
            },
            modifier = Modifier.size(dimensionResource(R.dimen.work_completed_bookmark_icon_size)),
        )
    }
}