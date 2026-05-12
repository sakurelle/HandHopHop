package ru.handhophop.feature.mash

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.handhophop.core.design.BackgroundPattern
import ru.handhophop.core.design.ButtonState
import ru.handhophop.core.design.HandHopHopButton
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.core.design.TopBar
import ru.handhophop.core.design.TopBarState
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
    onDeleteSheme: () -> Unit,
) {
    val metrics = uiState.toProjectMetrics()
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val contentPadding = dimensions.md
    val contentSpacing = dimensions.md
    val radius = dimensionResource(ru.handhophop.design.R.dimen.radius)
    val border = dimensionResource(ru.handhophop.design.R.dimen.border)

    var showDialog by rememberSaveable { mutableStateOf(false) }

    val dialogTitle = stringResource(R.string.dialog_warning_title)
    val dialogMessage = stringResource(R.string.dialog_warning_message)
    val dialogConfirm = stringResource(R.string.dialog_confirm)
    val dialogDismiss = stringResource(R.string.dialog_dismiss)


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            modifier = Modifier.border(
                width = border * 2,
                color = colors.button,
                shape = RoundedCornerShape(radius)
            ),
            title = {
                Text(
                    modifier = Modifier,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    text = dialogTitle
                )
            },
            text = {
                Text(
                    modifier = Modifier,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                    text = dialogMessage
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSheme()
                        showDialog = false
                    }
                ) {
                    Text(
                        modifier = Modifier,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                        text = dialogConfirm,
                        color = Color.Red,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text(
                        modifier = Modifier,
                        text = dialogDismiss,
                        color = colors.button
                    )
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(radius),
            )
    }
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
            TopBar(
                state = TopBarState(
                    projectName = projectConfig?.projectName
                        ?: stringResource(R.string.mash_home_screen_title),
                    titleRes = null,
                    leftIconRes = null,
                    rightIconRes = ru.handhophop.design.R.drawable.delete
                ),
                { showDialog = true },
                { Unit }
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
    val buttonCornerRadius = dimensions.md
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
