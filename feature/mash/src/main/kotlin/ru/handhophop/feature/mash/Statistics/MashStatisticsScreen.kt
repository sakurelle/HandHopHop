package ru.handhophop.feature.mash.Statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import ru.handhophop.core.design.BackgroundPattern
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.feature.mash.MashCreate.MashCreateConfig
import ru.handhophop.feature.mash.MashCreate.MashCreateSchemeType
import ru.handhophop.feature.mash.MashModuleTopBar
import ru.handhophop.feature.mash.MashUiState
import ru.handhophop.feature.mash.R

@Composable
internal fun MashStatisticsScreen(
    projectConfig: MashCreateConfig?,
    uiState: MashUiState,
    onBackClick: () -> Unit,
    onCreateProjectClick: () -> Unit,
    onOpenProjectClick: () -> Unit,
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
                title = stringResource(R.string.mash_statistics_screen_title),
                onBackClick = onBackClick,
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = contentPadding)
            ) {
                when {
                    projectConfig == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            MashStatisticsStateCard(
                                title = stringResource(R.string.mash_statistics_empty_title),
                                description = stringResource(R.string.mash_statistics_empty_description),
                                buttonText = stringResource(R.string.mash_home_create_project),
                                onButtonClick = onCreateProjectClick,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    uiState.isLoading && !metrics.isReady -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            MashStatisticsLoadingCard(modifier = Modifier.fillMaxWidth())
                        }
                    }

                    uiState.errorTextRes != null && !metrics.isReady -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            MashStatisticsStateCard(
                                title = stringResource(R.string.mash_statistics_unavailable_title),
                                description = stringResource(uiState.errorTextRes),
                                buttonText = stringResource(R.string.mash_home_new_project),
                                onButtonClick = onCreateProjectClick,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    metrics.isCompleted -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            MashStatisticsStateCard(
                                title = stringResource(R.string.mash_statistics_completed_title),
                                description = stringResource(R.string.mash_statistics_completed_description),
                                buttonText = stringResource(R.string.mash_home_new_project),
                                onButtonClick = onCreateProjectClick,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    !metrics.isReady -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            MashStatisticsStateCard(
                                title = stringResource(R.string.mash_statistics_waiting_title),
                                description = stringResource(R.string.mash_statistics_waiting_description),
                                buttonText = stringResource(R.string.mash_home_open_project),
                                onButtonClick = onOpenProjectClick,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = contentPadding),
                            verticalArrangement = Arrangement.spacedBy(contentSpacing)
                        ) {
                            MashStatisticsProjectCard(
                                projectConfig = projectConfig,
                                metrics = metrics,
                                onOpenProjectClick = onOpenProjectClick,
                            )
                            MashStatisticsActivityCard(
                                values = metrics.buildWeeklyActivity(projectConfig.difficulty),
                            )
                            MashStatisticsProgressCard(metrics = metrics)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MashStatisticsProjectCard(
    projectConfig: MashCreateConfig,
    metrics: MashProjectMetrics,
    onOpenProjectClick: () -> Unit,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    MashStatisticsCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(
                dimensions.sm
            )
        ) {
            Text(
                text = projectConfig.projectName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(projectDescription(projectConfig.schemeType)),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
            Text(
                text = stringResource(
                    R.string.mash_statistics_colors_subtitle,
                    metrics.completedUsedColors,
                    metrics.totalUsedColors
                ),
                style = MaterialTheme.typography.labelLarge,
                color = colors.primaryAction,
            )

            Button(
                onClick = onOpenProjectClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensionResource(R.dimen.mash_button_corner_radius)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryAction,
                    contentColor = colors.onPrimaryAction,
                )
            ) {
                Text(text = stringResource(R.string.mash_statistics_open_scheme_button))
            }
        }
    }
}

@Composable
private fun MashStatisticsActivityCard(
    values: List<Int>,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val dayLabels = listOf(
        stringResource(R.string.mash_weekday_mon),
        stringResource(R.string.mash_weekday_tue),
        stringResource(R.string.mash_weekday_wed),
        stringResource(R.string.mash_weekday_thu),
        stringResource(R.string.mash_weekday_fri),
        stringResource(R.string.mash_weekday_sat),
        stringResource(R.string.mash_weekday_sun),
    )

    MashStatisticsCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(
                dimensions.sm
            )
        ) {
            Text(
                text = stringResource(R.string.mash_statistics_activity_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.mash_statistics_activity_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val maxValue = (values.maxOrNull() ?: 0).coerceAtLeast(4)
                values.forEachIndexed { index, value ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            dimensions.sm
                        )
                    ) {
                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary,
                        )

                        Box(
                            modifier = Modifier
                                .height(dimensionResource(R.dimen.mash_statistics_chart_height))
                                .width(dimensionResource(R.dimen.mash_statistics_bar_width))
                                .clip(RoundedCornerShape(percent = 50))
                                .background(colors.surfaceSoft),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(
                                        dimensionResource(R.dimen.mash_statistics_chart_height) *
                                                (value.toFloat() / maxValue.toFloat())
                                    )
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(colors.primaryAction)
                            )
                        }

                        Text(
                            text = dayLabels[index],
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MashStatisticsProgressCard(
    metrics: MashProjectMetrics,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    MashStatisticsCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(
                dimensions.sm
            )
        ) {
            Text(
                text = stringResource(R.string.mash_statistics_progress_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    dimensions.md
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        dimensions.sm
                    )
                ) {
                    Text(
                        text = metrics.completedCells.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = stringResource(
                            R.string.mash_statistics_progress_subtitle,
                            metrics.completedCells,
                            metrics.totalCells
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                    )
                }

                MashCompletionRing(
                    metrics = metrics,
                    modifier = Modifier.size(dimensionResource(R.dimen.mash_statistics_ring_size))
                )
            }

            metrics.paletteUsage.take(4).forEach { usage ->
                MashPaletteUsageRow(usage = usage)
            }
        }
    }
}

@Composable
private fun MashCompletionRing(
    metrics: MashProjectMetrics,
    modifier: Modifier = Modifier,
) {
    val colors = HandHopHopDesignSystem.colors
    val strokeWidth = dimensionResource(R.dimen.mash_statistics_ring_stroke)
    val trackColor = colors.surfaceSoft

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
            )

            var startAngle = -90f
            metrics.paletteUsage.forEach { usage ->
                if (usage.completedCells <= 0) {
                    return@forEach
                }

                val sweep = 360f * usage.completedCells.toFloat() / metrics.totalCells.toFloat()
                drawArc(
                    color = usage.thread.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = stroke,
                )
                startAngle += sweep
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.mash_statistics_completion, metrics.progressPercent),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.mash_statistics_completion_caption),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun MashPaletteUsageRow(
    usage: MashPaletteUsage,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    Column(
        verticalArrangement = Arrangement.spacedBy(
            dimensions.sm
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    dimensions.sm
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.mash_create_thread_circle_size))
                        .clip(CircleShape)
                        .background(usage.thread.color)
                )
                Text(
                    text = usage.thread.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                )
            }

            Text(
                text = stringResource(
                    R.string.mash_statistics_progress_subtitle,
                    usage.completedCells,
                    usage.cells
                ),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.mash_statistics_palette_bar_height))
                .clip(RoundedCornerShape(percent = 50))
                .background(colors.surfaceSoft)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(
                        if (usage.cells == 0) {
                            0f
                        } else {
                            usage.completedCells.toFloat() / usage.cells.toFloat()
                        }
                    )
                    .height(dimensionResource(R.dimen.mash_statistics_palette_bar_height))
                    .clip(RoundedCornerShape(percent = 50))
                    .background(usage.thread.color)
            )
        }
    }
}

@Composable
private fun MashStatisticsStateCard(
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    MashStatisticsCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(
                dimensions.md
            )
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
            Button(
                onClick = onButtonClick,
                shape = RoundedCornerShape(dimensionResource(R.dimen.mash_button_corner_radius)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryAction,
                    contentColor = colors.onPrimaryAction,
                )
            ) {
                Text(text = buttonText)
            }
        }
    }
}

@Composable
private fun MashStatisticsLoadingCard(
    modifier: Modifier = Modifier,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    MashStatisticsCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                dimensions.sm
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(color = colors.primaryAction)
            Text(
                text = stringResource(R.string.mash_statistics_loading_title),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
            )
        }
    }
}

@Composable
private fun MashStatisticsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.mash_module_card_corner_radius)),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(R.dimen.mash_card_elevation)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(dimensions.md),
            verticalArrangement = Arrangement.spacedBy(
                dimensions.sm
            )
        ) {
            content()
        }
    }
}

private fun projectDescription(type: MashCreateSchemeType): Int {
    return when (type) {
        MashCreateSchemeType.COLORING -> R.string.mash_statistics_project_description_embroidery
        MashCreateSchemeType.EMBROIDERY -> R.string.mash_statistics_project_description_embroidery
    }
}
