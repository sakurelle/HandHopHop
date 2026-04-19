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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import ru.handhophop.core.design.BackgroundPattern
import ru.handhophop.feature.mash.MashCreate.MashCreateConfig
import ru.handhophop.feature.mash.MashCreate.MashCreateSchemeType
import ru.handhophop.feature.mash.MashUiState
import ru.handhophop.feature.mash.R

@Composable
internal fun MashStatisticsScreen(
    projectConfig: MashCreateConfig?,
    uiState: MashUiState,
    onCreateProjectClick: () -> Unit,
    onOpenProjectClick: () -> Unit,
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

        Column(modifier = Modifier.fillMaxSize()) {
            when {
                projectConfig == null -> {
                    MashStatisticsStateCard(
                        title = stringResource(R.string.mash_statistics_empty_title),
                        description = stringResource(R.string.mash_statistics_empty_description),
                        buttonText = stringResource(R.string.mash_home_create_project),
                        onButtonClick = onCreateProjectClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = contentPadding),
                    )
                }

                uiState.isLoading && !metrics.isReady -> {
                    MashStatisticsLoadingCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = contentPadding),
                    )
                }

                uiState.errorTextRes != null && !metrics.isReady -> {
                    MashStatisticsStateCard(
                        title = stringResource(R.string.mash_statistics_unavailable_title),
                        description = stringResource(uiState.errorTextRes),
                        buttonText = stringResource(R.string.mash_home_new_project),
                        onButtonClick = onCreateProjectClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = contentPadding),
                    )
                }

                metrics.isCompleted -> {
                    MashStatisticsStateCard(
                        title = stringResource(R.string.mash_statistics_completed_title),
                        description = stringResource(R.string.mash_statistics_completed_description),
                        buttonText = stringResource(R.string.mash_home_new_project),
                        onButtonClick = onCreateProjectClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = contentPadding),
                    )
                }

                !metrics.isReady -> {
                    MashStatisticsStateCard(
                        title = stringResource(R.string.mash_statistics_waiting_title),
                        description = stringResource(R.string.mash_statistics_waiting_description),
                        buttonText = stringResource(R.string.mash_home_open_project),
                        onButtonClick = onOpenProjectClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = contentPadding),
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = contentPadding),
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

@Composable
private fun MashStatisticsProjectCard(
    projectConfig: MashCreateConfig,
    metrics: MashProjectMetrics,
    onOpenProjectClick: () -> Unit,
) {
    MashStatisticsCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.mash_home_status_spacing)
            )
        ) {
            Text(
                text = projectConfig.projectName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.mash_text_primary),
            )
            Text(
                text = stringResource(projectDescription(projectConfig.schemeType)),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.mash_text_secondary),
            )
            Text(
                text = stringResource(
                    R.string.mash_statistics_colors_subtitle,
                    metrics.completedUsedColors,
                    metrics.totalUsedColors
                ),
                style = MaterialTheme.typography.labelLarge,
                color = colorResource(R.color.mash_primary),
            )

            Button(
                onClick = onOpenProjectClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensionResource(R.dimen.mash_button_corner_radius)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.mash_primary),
                    contentColor = colorResource(R.color.mash_white),
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
                dimensionResource(R.dimen.mash_home_status_spacing)
            )
        ) {
            Text(
                text = stringResource(R.string.mash_statistics_activity_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.mash_text_primary),
            )
            Text(
                text = stringResource(R.string.mash_statistics_activity_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.mash_text_secondary),
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
                            dimensionResource(R.dimen.mash_home_status_spacing)
                        )
                    ) {
                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = colorResource(R.color.mash_text_secondary),
                        )

                        Box(
                            modifier = Modifier
                                .height(dimensionResource(R.dimen.mash_statistics_chart_height))
                                .width(dimensionResource(R.dimen.mash_statistics_bar_width))
                                .clip(RoundedCornerShape(percent = 50))
                                .background(colorResource(R.color.mash_surface_soft)),
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
                                    .background(colorResource(R.color.mash_primary))
                            )
                        }

                        Text(
                            text = dayLabels[index],
                            style = MaterialTheme.typography.labelMedium,
                            color = colorResource(R.color.mash_text_secondary),
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
    MashStatisticsCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.mash_home_status_spacing)
            )
        ) {
            Text(
                text = stringResource(R.string.mash_statistics_progress_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.mash_text_primary),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.mash_module_section_spacing)
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        dimensionResource(R.dimen.mash_home_status_spacing)
                    )
                ) {
                    Text(
                        text = metrics.completedCells.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        color = colorResource(R.color.mash_text_primary),
                    )
                    Text(
                        text = stringResource(
                            R.string.mash_statistics_progress_subtitle,
                            metrics.completedCells,
                            metrics.totalCells
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(R.color.mash_text_secondary),
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
    val strokeWidth = dimensionResource(R.dimen.mash_statistics_ring_stroke)
    val trackColor = colorResource(R.color.mash_surface_soft)

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
                color = colorResource(R.color.mash_text_primary),
            )
            Text(
                text = stringResource(R.string.mash_statistics_completion_caption),
                style = MaterialTheme.typography.labelMedium,
                color = colorResource(R.color.mash_text_secondary),
            )
        }
    }
}

@Composable
private fun MashPaletteUsageRow(
    usage: MashPaletteUsage,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.mash_home_status_spacing)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.mash_home_status_spacing)
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
                    color = colorResource(R.color.mash_text_primary),
                )
            }

            Text(
                text = stringResource(
                    R.string.mash_statistics_progress_subtitle,
                    usage.completedCells,
                    usage.cells
                ),
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(R.color.mash_text_secondary),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.mash_statistics_palette_bar_height))
                .clip(RoundedCornerShape(percent = 50))
                .background(colorResource(R.color.mash_surface_soft))
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
    MashStatisticsCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.mash_module_section_spacing)
            )
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
    MashStatisticsCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.mash_home_status_spacing)
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(color = colorResource(R.color.mash_primary))
            Text(
                text = stringResource(R.string.mash_statistics_loading_title),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(R.color.mash_text_primary),
            )
        }
    }
}

@Composable
private fun MashStatisticsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.mash_module_card_corner_radius)),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.mash_surface)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(R.dimen.mash_card_elevation)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.mash_surface))
                .padding(dimensionResource(R.dimen.mash_module_card_padding)),
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.mash_home_status_spacing)
            )
        ) {
            content()
        }
    }
}

private fun projectDescription(type: MashCreateSchemeType): Int {
    return when (type) {
        MashCreateSchemeType.COLORING -> R.string.mash_statistics_project_description_coloring
        MashCreateSchemeType.EMBROIDERY -> R.string.mash_statistics_project_description_embroidery
    }
}
