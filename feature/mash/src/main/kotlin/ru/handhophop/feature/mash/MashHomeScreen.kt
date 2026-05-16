package ru.handhophop.feature.mash

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import ru.handhophop.core.design.BackgroundPattern
import ru.handhophop.core.design.ButtonState
import ru.handhophop.core.design.HandHopHopButton
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.feature.mash.MashCreate.MashCreateConfig
import ru.handhophop.feature.mash.Statistics.MashProjectMetrics
import ru.handhophop.feature.mash.Statistics.toProjectMetrics
import ru.handhophop.design.R as DesignR

@Composable
internal fun MashHomeScreen(
    projectConfig: MashCreateConfig?,
    uiState: MashUiState,
    onCreateProjectClick: () -> Unit,
    onOpenProjectClick: () -> Unit,
    onOpenStatisticsClick: () -> Unit,
) {
    val metrics = uiState.toProjectMetrics()
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val contentPadding = dimensions.md
    val contentSpacing = dimensions.md

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        BackgroundPattern()

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(contentSpacing),
        ) {
            MashModuleTopBar(
                title = projectConfig?.projectName ?: stringResource(R.string.mash_home_screen_title),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(
                        start = contentPadding,
                        end = contentPadding,
                        bottom = contentPadding,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(contentSpacing),
                ) {
                    when {
                        projectConfig == null -> MashHomeStateCard(
                            title = stringResource(R.string.mash_home_no_project_title),
                            description = stringResource(R.string.mash_home_no_project_description),
                            buttonText = stringResource(R.string.mash_home_create_project),
                            onButtonClick = onCreateProjectClick,
                        )

                        uiState.isLoading && !metrics.isReady -> MashHomeStateCard(
                            title = stringResource(R.string.mash_home_loading_title),
                            description = stringResource(R.string.mash_home_loading_description),
                            buttonText = stringResource(R.string.mash_home_open_project),
                            onButtonClick = onOpenProjectClick,
                        )

                        uiState.errorTextRes != null && !metrics.isReady -> MashHomeStateCard(
                            title = stringResource(R.string.mash_statistics_unavailable_title),
                            description = stringResource(R.string.mash_home_error_description),
                            buttonText = stringResource(R.string.mash_home_new_project),
                            onButtonClick = onCreateProjectClick,
                        )

                        else -> MashCurrentWorkCard(
                            projectConfig = projectConfig,
                            metrics = metrics,
                            onOpenProjectClick = onOpenProjectClick,
                            onOpenStatisticsClick = onOpenStatisticsClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun MashModuleTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val horizontalPadding = dimensions.md
    val verticalPadding = dimensions.xs
    val sideWidth = dimensions.xl * 2

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    bottomStart = dimensions.lg,
                    bottomEnd = dimensions.lg,
                )
            )
            .background(colors.topBar)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = verticalPadding,
                    bottom = verticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier.size(sideWidth),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (onBackClick != null) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(DesignR.drawable.arrow),
                            contentDescription = stringResource(R.string.mash_navigation_back),
                            tint = colors.textPrimary,
                            modifier = Modifier.rotate(180f),
                        )
                    }
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = horizontalPadding),
            )

            SpacerSlot(sideWidth = sideWidth)
        }
    }
}

@Composable
private fun SpacerSlot(
    sideWidth: androidx.compose.ui.unit.Dp,
) {
    Box(modifier = Modifier.size(sideWidth))
}

@Composable
private fun MashCurrentWorkCard(
    projectConfig: MashCreateConfig,
    metrics: MashProjectMetrics,
    onOpenProjectClick: () -> Unit,
    onOpenStatisticsClick: () -> Unit,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val cardCornerRadius = dimensions.lg
    val borderWidth = dimensions.xs / 4
    val imageModifier = Modifier
        .fillMaxWidth(0.52f)
        .aspectRatio(1f)
        .clip(CircleShape)
    val progressText = when {
        metrics.isCompleted -> stringResource(R.string.mash_home_completed_description)
        metrics.completedCells == 0 -> stringResource(R.string.mash_home_not_started_description)
        else -> stringResource(
            R.string.mash_home_in_progress_description,
            metrics.progressPercent,
            metrics.totalCells - metrics.completedCells,
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            dimensions.sm,
        ),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(cardCornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = dimensions.xs,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensions.md),
                verticalArrangement = Arrangement.spacedBy(
                    dimensions.sm,
                ),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (projectConfig.imageUrl.isNullOrBlank()) {
                        Box(
                            modifier = imageModifier
                                .background(colors.surfaceSoft)
                                .border(
                                    width = borderWidth,
                                    color = colors.notWhite,
                                    shape = CircleShape,
                                ),
                        )
                    } else {
                        AsyncImage(
                            model = projectConfig.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = imageModifier
                                .border(
                                    width = borderWidth,
                                    color = colors.notWhite,
                                    shape = CircleShape,
                                ),
                        )
                    }
                }

                Text(
                    text = progressText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )

                MashInfoRow(projectConfig = projectConfig, metrics = metrics)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            HandHopHopButton(
                onClick = onOpenStatisticsClick,
                size = ButtonState.Size.FILL,
                textColor = ButtonState.Color.Button,
                buttonColor = ButtonState.Color.Background,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.mash_home_open_statistics),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = ">",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                    )
                }
            }
        }

        HandHopHopButton(
            onClick = onOpenProjectClick,
            size = ButtonState.Size.FILL,
            textColor = ButtonState.Color.White,
            buttonColor = ButtonState.Color.Button,
        ) {
            Text(
                text = if (metrics.completedCells == 0) {
                    stringResource(R.string.Start)
                } else {
                    stringResource(R.string.mash_home_open_project)
                },
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun MashInfoRow(
    projectConfig: MashCreateConfig,
    metrics: MashProjectMetrics,
) {
    val dimensions = HandHopHopDesignSystem.dimensions
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            dimensions.sm,
        ),
    ) {
        MashInfoChip(
            title = stringResource(R.string.mash_home_info_colors),
            value = metrics.totalUsedColors.toString(),
            modifier = Modifier.weight(1f),
        )
        MashInfoChip(
            title = stringResource(R.string.mash_home_info_difficulty),
            value = stringResource(projectConfig.difficulty.titleRes),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MashInfoChip(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.md))
            .background(colors.surfaceSoft)
            .padding(dimensions.sm),
        verticalArrangement = Arrangement.spacedBy(dimensions.xs),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun MashHomeStateCard(
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.lg),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensions.xs,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.md),
            verticalArrangement = Arrangement.spacedBy(
                dimensions.md,
            ),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
            HandHopHopButton(
                onClick = onButtonClick,
                size = ButtonState.Size.WRAPCONTENT,
                textColor = ButtonState.Color.White,
                buttonColor = ButtonState.Color.Button,
            ) {
                Text(text = buttonText)
            }
        }
    }
}
