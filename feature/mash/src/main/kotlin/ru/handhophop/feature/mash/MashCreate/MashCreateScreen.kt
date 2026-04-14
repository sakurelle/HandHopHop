package ru.handhophop.feature.mash.MashCreate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import kotlin.math.roundToInt
import ru.handhophop.core.design.BackgroundPattern
import ru.handhophop.feature.mash.R

@Composable
internal fun MashCreateScreen(
    imageUrl: String?,
    suggestedProjectName: String = "",
    onBackClick: () -> Unit = {},
    onCreateFinished: (MashCreateConfig) -> Unit = {},
    topBar: @Composable (
        Boolean,
        () -> Unit,
        () -> Unit,
    ) -> Unit,
) {
    var projectName by rememberSaveable(imageUrl, suggestedProjectName) {
        mutableStateOf(suggestedProjectName)
    }
    var schemeType by rememberSaveable(imageUrl, suggestedProjectName) {
        mutableStateOf(MashCreateSchemeType.COLORING)
    }
    var colorCount by rememberSaveable(imageUrl, suggestedProjectName) {
        mutableIntStateOf(MASH_CREATE_DEFAULT_COLORS)
    }
    var difficulty by rememberSaveable(imageUrl, suggestedProjectName) {
        mutableStateOf(MashCreateDifficulty.MEDIUM)
    }

    val threads = MashCreateData.getThreadsByCount(colorCount)
    val isCreateButtonEnabled = projectName.isNotBlank()

    fun createWork() {
        if (!isCreateButtonEnabled) return

        onCreateFinished(
            MashCreateConfig(
                projectName = projectName.trim(),
                imageUrl = imageUrl,
                schemeType = schemeType,
                colorCount = colorCount,
                difficulty = difficulty,
                threads = threads,
            )
        )
    }

    MashCreateContent(
        projectName = projectName,
        imageUrl = imageUrl,
        schemeType = schemeType,
        colorCount = colorCount,
        difficulty = difficulty,
        threads = threads,
        isCreateButtonEnabled = isCreateButtonEnabled,
        onProjectNameChanged = { projectName = it },
        onClearProjectNameClick = { projectName = "" },
        onSchemeTypeSelected = { schemeType = it },
        onColorCountChanged = {
            colorCount = it.coerceIn(MASH_CREATE_MIN_COLORS, MASH_CREATE_MAX_COLORS)
        },
        onDifficultyChanged = { difficulty = it },
        onCreateClick = ::createWork,
        onBackClick = onBackClick,
        topBar = topBar,
    )
}

@Composable
private fun MashCreateContent(
    projectName: String,
    imageUrl: String?,
    schemeType: MashCreateSchemeType,
    colorCount: Int,
    difficulty: MashCreateDifficulty,
    threads: List<MashThread>,
    isCreateButtonEnabled: Boolean,
    onProjectNameChanged: (String) -> Unit,
    onClearProjectNameClick: () -> Unit,
    onSchemeTypeSelected: (MashCreateSchemeType) -> Unit,
    onColorCountChanged: (Int) -> Unit,
    onDifficultyChanged: (MashCreateDifficulty) -> Unit,
    onCreateClick: () -> Unit,
    onBackClick: () -> Unit,
    topBar: @Composable (
        Boolean,
        () -> Unit,
        () -> Unit,
    ) -> Unit,
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.mash_background))
    ) {
        BackgroundPattern()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            topBar(
                isCreateButtonEnabled,
                onBackClick,
                onCreateClick,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        horizontal = dimensionResource(R.dimen.mash_create_screen_horizontal_padding),
                        vertical = dimensionResource(R.dimen.mash_create_screen_vertical_padding),
                    ),
                verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.mash_create_screen_content_spacing)
                )
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(
                        dimensionResource(R.dimen.mash_create_card_corner_radius)
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorResource(R.color.mash_surface))
                            .padding(dimensionResource(R.dimen.mash_create_card_padding)),
                        verticalArrangement = Arrangement.spacedBy(
                            dimensionResource(R.dimen.mash_create_section_spacing)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.mash_create_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(R.color.mash_text_primary),
                        )

                        MashCreateProjectNameSection(
                            value = projectName,
                            onValueChanged = onProjectNameChanged,
                            onClearClick = onClearProjectNameClick,
                        )

                        MashCreateImageSection(imageUrl = imageUrl)

                        MashCreateSchemeTypeSection(
                            selectedType = schemeType,
                            onTypeSelected = onSchemeTypeSelected,
                        )

                        MashCreateColorsSection(
                            colorCount = colorCount,
                            threads = threads,
                            onColorCountChanged = onColorCountChanged,
                        )

                        MashCreateDifficultySection(
                            difficulty = difficulty,
                            onDifficultyChanged = onDifficultyChanged,
                        )

                        Button(
                            onClick = onCreateClick,
                            enabled = isCreateButtonEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensionResource(R.dimen.mash_create_button_height)),
                            shape = RoundedCornerShape(
                                dimensionResource(R.dimen.mash_create_button_corner_radius)
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.mash_primary),
                                contentColor = colorResource(R.color.mash_white),
                                disabledContainerColor = colorResource(R.color.mash_primary_disabled),
                                disabledContentColor = colorResource(R.color.mash_text_secondary),
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.mash_create_button_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MashCreateProjectNameSection(
    value: String,
    onValueChanged: (String) -> Unit,
    onClearClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.mash_create_inner_section_spacing)
        )
    ) {
        Text(
            text = stringResource(R.string.mash_create_project_name_label),
            style = MaterialTheme.typography.titleMedium,
            color = colorResource(R.color.mash_text_primary),
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            placeholder = {
                Text(
                    text = stringResource(R.string.mash_create_project_name_placeholder)
                )
            },
            trailingIcon = {
                if (value.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.mash_create_clear_field),
                        modifier = Modifier.clickable(onClick = onClearClick),
                        color = colorResource(R.color.mash_text_primary),
                    )
                }
            },
            shape = RoundedCornerShape(
                dimensionResource(R.dimen.mash_create_text_field_corner_radius)
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colorResource(R.color.mash_surface_soft),
                unfocusedContainerColor = colorResource(R.color.mash_surface_soft),
                focusedBorderColor = colorResource(R.color.mash_primary),
                unfocusedBorderColor = colorResource(R.color.mash_primary),
                focusedTextColor = colorResource(R.color.mash_text_primary),
                unfocusedTextColor = colorResource(R.color.mash_text_primary),
                focusedPlaceholderColor = colorResource(R.color.mash_text_secondary),
                unfocusedPlaceholderColor = colorResource(R.color.mash_text_secondary),
                focusedTrailingIconColor = colorResource(R.color.mash_text_primary),
                unfocusedTrailingIconColor = colorResource(R.color.mash_text_primary),
                cursorColor = colorResource(R.color.mash_text_primary),
            )
        )
    }
}

@Composable
private fun MashCreateImageSection(
    imageUrl: String?,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.mash_create_inner_section_spacing)
        )
    ) {
        Text(
            text = stringResource(R.string.mash_create_image_label),
            style = MaterialTheme.typography.titleMedium,
            color = colorResource(R.color.mash_text_primary),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.mash_create_image_height))
                .clip(
                    RoundedCornerShape(
                        dimensionResource(R.dimen.mash_create_image_corner_radius)
                    )
                )
                .background(colorResource(R.color.mash_surface_soft))
                .border(
                    width = dimensionResource(R.dimen.mash_create_image_border_width),
                    color = colorResource(R.color.mash_outline),
                    shape = RoundedCornerShape(
                        dimensionResource(R.dimen.mash_create_image_corner_radius)
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (imageUrl.isNullOrBlank()) {
                    stringResource(R.string.mash_create_image_placeholder)
                } else {
                    stringResource(R.string.mash_create_image_selected)
                },
                color = colorResource(R.color.mash_text_secondary),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun MashCreateSchemeTypeSection(
    selectedType: MashCreateSchemeType,
    onTypeSelected: (MashCreateSchemeType) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.mash_create_inner_section_spacing)
        )
    ) {
        Text(
            text = stringResource(R.string.mash_create_scheme_type_label),
            style = MaterialTheme.typography.titleMedium,
            color = colorResource(R.color.mash_text_primary),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.mash_create_toggle_spacing)
            )
        ) {
            MashCreateSchemeToggle(
                title = stringResource(R.string.mash_create_scheme_coloring),
                isSelected = selectedType == MashCreateSchemeType.COLORING,
                onClick = { onTypeSelected(MashCreateSchemeType.COLORING) },
                modifier = Modifier.weight(1f),
            )

            MashCreateSchemeToggle(
                title = stringResource(R.string.mash_create_scheme_embroidery),
                isSelected = selectedType == MashCreateSchemeType.EMBROIDERY,
                onClick = { onTypeSelected(MashCreateSchemeType.EMBROIDERY) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MashCreateSchemeToggle(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (isSelected) {
        colorResource(R.color.mash_primary)
    } else {
        colorResource(R.color.mash_surface_soft)
    }

    val border = if (isSelected) {
        colorResource(R.color.mash_primary)
    } else {
        colorResource(R.color.mash_outline)
    }

    val textColor = if (isSelected) {
        colorResource(R.color.mash_white)
    } else {
        colorResource(R.color.mash_text_primary)
    }

    Box(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    dimensionResource(R.dimen.mash_create_toggle_corner_radius)
                )
            )
            .background(background)
            .border(
                width = dimensionResource(R.dimen.mash_create_toggle_border_width),
                color = border,
                shape = RoundedCornerShape(
                    dimensionResource(R.dimen.mash_create_toggle_corner_radius)
                )
            )
            .clickable(onClick = onClick)
            .padding(
                vertical = dimensionResource(R.dimen.mash_create_toggle_vertical_padding)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
        )
    }
}

@Composable
private fun MashCreateColorsSection(
    colorCount: Int,
    threads: List<MashThread>,
    onColorCountChanged: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.mash_create_inner_section_spacing)
        )
    ) {
        Text(
            text = stringResource(R.string.mash_create_color_count_title, colorCount),
            style = MaterialTheme.typography.titleMedium,
            color = colorResource(R.color.mash_text_primary),
        )

        Slider(
            value = colorCount.toFloat(),
            onValueChange = { value ->
                onColorCountChanged(value.roundToInt())
            },
            valueRange = MASH_CREATE_MIN_COLORS.toFloat()..MASH_CREATE_MAX_COLORS.toFloat(),
            steps = MASH_CREATE_MAX_COLORS - MASH_CREATE_MIN_COLORS - 1,
            colors = SliderDefaults.colors(
                thumbColor = colorResource(R.color.mash_primary),
                activeTrackColor = colorResource(R.color.mash_primary),
                inactiveTrackColor = colorResource(R.color.mash_surface_soft),
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.mash_create_color_count_min),
                color = colorResource(R.color.mash_text_secondary),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.mash_create_color_count_max),
                color = colorResource(R.color.mash_text_secondary),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.mash_create_threads_spacing)
            )
        ) {
            threads.take(8).forEach { thread ->
                MashCreateThreadPreview(thread = thread)
            }
        }
    }
}

@Composable
private fun MashCreateThreadPreview(
    thread: MashThread,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.mash_create_thread_item_spacing)
        )
    ) {
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.mash_create_thread_circle_size))
                .background(thread.color, CircleShape)
                .border(
                    width = dimensionResource(R.dimen.mash_create_thread_circle_border_width),
                    color = colorResource(R.color.mash_outline),
                    shape = CircleShape
                )
        )

        Text(
            text = thread.article,
            style = MaterialTheme.typography.labelSmall,
            color = colorResource(R.color.mash_text_secondary),
        )
    }
}

@Composable
private fun MashCreateDifficultySection(
    difficulty: MashCreateDifficulty,
    onDifficultyChanged: (MashCreateDifficulty) -> Unit,
) {
    val difficultySteps = MashCreateDifficulty.entries.size - 2
    val difficultyLastIndex = MashCreateDifficulty.entries.lastIndex

    Column(
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.mash_create_inner_section_spacing)
        )
    ) {
        Text(
            text = stringResource(
                R.string.mash_create_difficulty_title,
                stringResource(difficulty.titleRes),
                difficulty.minSidePx
            ),
            style = MaterialTheme.typography.titleMedium,
            color = colorResource(R.color.mash_text_primary),
        )

        Slider(
            value = difficulty.ordinal.toFloat(),
            onValueChange = { value ->
                val newDifficulty =
                    MashCreateDifficulty.entries[value.roundToInt().coerceIn(0, difficultyLastIndex)]
                onDifficultyChanged(newDifficulty)
            },
            valueRange = 0f..difficultyLastIndex.toFloat(),
            steps = difficultySteps,
            colors = SliderDefaults.colors(
                thumbColor = colorResource(R.color.mash_primary),
                activeTrackColor = colorResource(R.color.mash_primary),
                inactiveTrackColor = colorResource(R.color.mash_surface_soft),
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MashCreateDifficulty.entries.forEach { level ->
                Text(
                    text = stringResource(level.titleRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(R.color.mash_text_secondary),
                )
            }
        }
    }
}
