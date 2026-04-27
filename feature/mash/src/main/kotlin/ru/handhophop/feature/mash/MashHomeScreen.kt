package ru.handhophop.feature.mash

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.handhophop.core.design.BackgroundPattern
import ru.handhophop.feature.mash.MashCreate.MashCreateConfig
import ru.handhophop.feature.mash.Statistics.MashProjectMetrics
import ru.handhophop.feature.mash.Statistics.toProjectMetrics

@Composable
internal fun MashHomeScreen(
    projectConfig: MashCreateConfig?,
    uiState: MashUiState,
    onCreateProjectClick: () -> Unit,
    onOpenProjectClick: () -> Unit,
    onOpenStatisticsClick: () -> Unit,
) {
    val metrics = uiState.toProjectMetrics()
    val contentPadding = dimensionResource(R.dimen.mash_module_content_padding)
    val contentSpacing = dimensionResource(R.dimen.mash_module_section_spacing)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.mash_background))
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
    val horizontalPadding = dimensionResource(R.dimen.mash_module_top_bar_horizontal_padding)
    val verticalPadding = dimensionResource(R.dimen.mash_module_top_bar_vertical_padding)
    val sideWidth = dimensionResource(R.dimen.mash_module_top_bar_side_width)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    bottomStart = dimensionResource(R.dimen.main_radius),
                    bottomEnd = dimensionResource(R.dimen.main_radius),
                )
            )
            .background(colorResource(R.color.main_color))
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
                            painter = painterResource(R.drawable.arrow),
                            contentDescription = stringResource(R.string.mash_navigation_back),
                            tint = colorResource(R.color.mash_text_primary),
                            modifier = Modifier.rotate(180f),
                        )
                    }
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.mash_text_primary),
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
            dimensionResource(R.dimen.mash_home_status_spacing),
        ),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensionResource(R.dimen.mash_module_card_corner_radius)),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.mash_surface),
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = dimensionResource(R.dimen.mash_card_elevation),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.mash_module_card_padding)),
                verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.mash_home_status_spacing),
                ),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (projectConfig.imageUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(dimensionResource(R.dimen.avatar_size))
                                .clip(CircleShape)
                                .background(colorResource(R.color.mash_surface_soft))
                                .border(
                                    width = dimensionResource(R.dimen.border_width),
                                    color = colorResource(R.color.mash_white),
                                    shape = CircleShape,
                                ),
                        )
                    } else {
                        AsyncImage(
                            model = projectConfig.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(dimensionResource(R.dimen.avatar_size))
                                .clip(CircleShape)
                                .border(
                                    width = dimensionResource(R.dimen.border_width),
                                    color = colorResource(R.color.mash_white),
                                    shape = CircleShape,
                                ),
                        )
                    }
                }

                Text(
                    text = progressText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.mash_text_secondary),
                )

                MashInfoRow(projectConfig = projectConfig, metrics = metrics)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.mash_button_height))
                .clip(RoundedCornerShape(dimensionResource(R.dimen.mash_button_corner_radius)))
                .background(colorResource(R.color.mash_surface))
                .clickable(onClick = onOpenStatisticsClick)
                .padding(horizontal = dimensionResource(R.dimen.mash_module_card_padding)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.mash_home_open_statistics),
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(R.color.mash_text_primary),
            )
            Text(
                text = ">",
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(R.color.mash_text_primary),
            )
        }

        Button(
            onClick = onOpenProjectClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.mash_button_height)),
            shape = RoundedCornerShape(dimensionResource(R.dimen.mash_button_corner_radius)),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.mash_primary),
                contentColor = colorResource(R.color.mash_white),
            ),
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.mash_home_status_spacing),
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(dimensionResource(R.dimen.mash_button_corner_radius)))
            .background(colorResource(R.color.mash_surface_soft))
            .padding(dimensionResource(R.dimen.mash_home_status_spacing)),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = colorResource(R.color.mash_text_secondary),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = colorResource(R.color.mash_text_primary),
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.mash_module_card_corner_radius)),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.mash_surface),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(R.dimen.mash_card_elevation),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.mash_module_card_padding)),
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.mash_module_section_spacing),
            ),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.mash_text_primary),
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.mash_text_secondary),
            )
            Button(
                onClick = onButtonClick,
                shape = RoundedCornerShape(dimensionResource(R.dimen.mash_button_corner_radius)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.mash_primary),
                    contentColor = colorResource(R.color.mash_white),
                ),
            ) {
                Text(text = buttonText)
            }
        }
    }
}
