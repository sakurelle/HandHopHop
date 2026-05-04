package ru.handhophop.feature.mash.MashCreate

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalContext
import kotlin.math.roundToInt
import ru.handhophop.core.design.BackgroundPattern
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.core.design.TopBar
import ru.handhophop.core.design.TopBarState
import ru.handhophop.feature.mash.R
import ru.handhophop.feature.mash.loadBitmapFromUrl
import ru.handhophop.feature.mash.selectPaletteForImage

@Composable
internal fun MashCreateScreen(
    imageUrl: String?,
    suggestedProjectName: String = "",
    onBackClick: () -> Unit = {},
    onCreateFinished: (MashCreateConfig) -> Unit = {},
) {
    var projectName by rememberSaveable(imageUrl, suggestedProjectName) {
        mutableStateOf(suggestedProjectName)
    }
    var colorCount by rememberSaveable(imageUrl, suggestedProjectName) {
        mutableIntStateOf(MASH_CREATE_DEFAULT_COLORS)
    }
    var difficulty by rememberSaveable(imageUrl, suggestedProjectName) {
        mutableStateOf(MashCreateDifficulty.MEDIUM)
    }
    val context = LocalContext.current
    val previewBitmap by produceState<Bitmap?>(
        initialValue = null,
        key1 = imageUrl,
    ) {
        value = if (imageUrl.isNullOrBlank()) {
            null
        } else {
            loadBitmapFromUrl(
                context = context,
                url = imageUrl,
            )
        }
    }

    val previewThreads = previewBitmap?.let { bitmap ->
        selectPaletteForImage(
            source = bitmap,
            minSideCells = difficulty.minSidePx,
            availablePalette = MashCreateData.allThreads,
            maxColors = colorCount,
        )
    } ?: MashCreateData.getThreadsByCount(colorCount)
    val isCreateButtonEnabled = projectName.isNotBlank()

    fun createWork() {
        if (!isCreateButtonEnabled) return

        onCreateFinished(
            MashCreateConfig(
                projectName = projectName.trim(),
                imageUrl = imageUrl,
                schemeType = MashCreateSchemeType.EMBROIDERY,
                colorCount = colorCount,
                difficulty = difficulty,
                threads = previewThreads,
            )
        )
    }

    MashCreateContent(
        projectName = projectName,
        previewBitmap = previewBitmap,
        colorCount = colorCount,
        difficulty = difficulty,
        threads = previewThreads,
        isCreateButtonEnabled = isCreateButtonEnabled,
        onBackClick = onBackClick,
        onProjectNameChanged = { projectName = it },
        onClearProjectNameClick = { projectName = "" },
        onColorCountChanged = {
            colorCount = it.coerceIn(MASH_CREATE_MIN_COLORS, MASH_CREATE_MAX_COLORS)
        },
        onDifficultyChanged = { difficulty = it },
        onCreateClick = ::createWork,
    )
}

@Composable
private fun MashCreateContent(
    projectName: String,
    previewBitmap: Bitmap?,
    colorCount: Int,
    difficulty: MashCreateDifficulty,
    threads: List<MashThread>,
    isCreateButtonEnabled: Boolean,
    onBackClick: () -> Unit,
    onProjectNameChanged: (String) -> Unit,
    onClearProjectNameClick: () -> Unit,
    onColorCountChanged: (Int) -> Unit,
    onDifficultyChanged: (MashCreateDifficulty) -> Unit,
    onCreateClick: () -> Unit,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        BackgroundPattern()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            TopBar(
                state = TopBarState(
                    titleRes = R.string.mash_create_screen_title,
                    leftIconRes = R.drawable.arrow,
                    null
                ),
                onClickLeft = onBackClick,
                onClickRight = {Unit}

            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        horizontal = dimensions.md,
                        vertical = dimensions.sm,
                    ),
                verticalArrangement = Arrangement.spacedBy(
                    dimensions.sm
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
                            .background(colors.surface)
                            .padding(dimensions.lg),
                        verticalArrangement = Arrangement.spacedBy(
                            dimensions.md
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.mash_create_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                        )

                        MashCreateProjectNameSection(
                            value = projectName,
                            onValueChanged = onProjectNameChanged,
                            onClearClick = onClearProjectNameClick,
                        )

                        MashCreateImageSection(previewBitmap = previewBitmap)

                        MashCreateSchemeTypeSection()

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
                                containerColor = colors.primaryAction,
                                contentColor = colors.onPrimaryAction,
                                disabledContainerColor = colors.primaryActionDisabled,
                                disabledContentColor = colors.textSecondary,
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
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    Column(
        verticalArrangement = Arrangement.spacedBy(
            dimensions.sm
        )
    ) {
        Text(
            text = stringResource(R.string.mash_create_project_name_label),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
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
                        color = colors.textPrimary,
                    )
                }
            },
            shape = RoundedCornerShape(
                dimensionResource(R.dimen.mash_create_text_field_corner_radius)
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceSoft,
                unfocusedContainerColor = colors.surfaceSoft,
                focusedBorderColor = colors.primaryAction,
                unfocusedBorderColor = colors.primaryAction,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                focusedPlaceholderColor = colors.textSecondary,
                unfocusedPlaceholderColor = colors.textSecondary,
                focusedTrailingIconColor = colors.textPrimary,
                unfocusedTrailingIconColor = colors.textPrimary,
                cursorColor = colors.textPrimary,
            )
        )
    }
}

@Composable
private fun MashCreateImageSection(
    previewBitmap: Bitmap?,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    Column(
        verticalArrangement = Arrangement.spacedBy(
            dimensions.sm
        )
    ) {
        Text(
            text = stringResource(R.string.mash_create_image_label),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        dimensionResource(R.dimen.mash_create_image_corner_radius)
                    )
                )
                .background(colors.surfaceSoft)
                .border(
                    width = dimensionResource(R.dimen.mash_create_image_border_width),
                    color = colors.outline,
                    shape = RoundedCornerShape(
                        dimensionResource(R.dimen.mash_create_image_corner_radius)
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (previewBitmap == null) {
                Text(
                    text = stringResource(R.string.mash_create_image_placeholder),
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.mash_create_image_height))
                        .padding(dimensions.lg),
                )
            } else {
                val aspectRatio = previewBitmap.width.toFloat() / previewBitmap.height.toFloat()

                Image(
                    bitmap = previewBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio),
                )
            }
        }
    }
}

@Composable
private fun MashCreateSchemeTypeSection(
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    Column(
        verticalArrangement = Arrangement.spacedBy(
            dimensions.sm
        )
    ) {
        Text(
            text = stringResource(R.string.mash_create_scheme_type_label),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                dimensions.sm
            )
        ) {
            MashCreateSchemeTypeChip(
                title = stringResource(R.string.mash_create_scheme_coloring),
                isEnabled = false,
                isSelected = false,
            )
            MashCreateSchemeTypeChip(
                title = stringResource(R.string.mash_create_scheme_embroidery),
                isEnabled = true,
                isSelected = true,
            )
        }

        Text(
            text = stringResource(R.string.mash_create_scheme_locked_hint),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun RowScope.MashCreateSchemeTypeChip(
    title: String,
    isEnabled: Boolean,
    isSelected: Boolean,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val shape = RoundedCornerShape(
        dimensionResource(R.dimen.mash_create_toggle_corner_radius)
    )
    val backgroundColor = when {
        isSelected -> colors.primaryAction
        isEnabled -> colors.surfaceSoft
        else -> colors.primaryActionDisabled
    }
    val borderColor = if (isSelected) {
        colors.primaryAction
    } else {
        colors.outline
    }
    val textColor = if (isSelected) {
        colors.onPrimaryAction
    } else {
        colors.textSecondary
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = dimensionResource(R.dimen.mash_create_toggle_border_width),
                color = borderColor,
                shape = shape,
            )
            .padding(
                vertical = dimensions.sm
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
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
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    Column(
        verticalArrangement = Arrangement.spacedBy(
            dimensions.sm
        )
    ) {
        Text(
            text = stringResource(R.string.mash_create_color_count_title, colorCount),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
        )

        Slider(
            value = colorCount.toFloat(),
            onValueChange = { value ->
                onColorCountChanged(value.roundToInt())
            },
            valueRange = MASH_CREATE_MIN_COLORS.toFloat()..MASH_CREATE_MAX_COLORS.toFloat(),
            steps = MASH_CREATE_MAX_COLORS - MASH_CREATE_MIN_COLORS - 1,
            colors = SliderDefaults.colors(
                thumbColor = colors.primaryAction,
                activeTrackColor = colors.primaryAction,
                inactiveTrackColor = colors.surfaceSoft,
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.mash_create_color_count_min),
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.mash_create_color_count_max),
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(
                dimensions.sm
            )
        ) {
            threads.forEach { thread ->
                MashCreateThreadPreview(thread = thread)
            }
        }
    }
}

@Composable
private fun MashCreateThreadPreview(
    thread: MashThread,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            dimensions.xs
        )
    ) {
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.mash_create_thread_circle_size))
                .background(thread.color, CircleShape)
                .border(
                    width = dimensionResource(R.dimen.mash_create_thread_circle_border_width),
                    color = colors.outline,
                    shape = CircleShape
                )
        )

        Text(
            text = thread.article,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun MashCreateDifficultySection(
    difficulty: MashCreateDifficulty,
    onDifficultyChanged: (MashCreateDifficulty) -> Unit,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val difficultySteps = MashCreateDifficulty.entries.size - 2
    val difficultyLastIndex = MashCreateDifficulty.entries.lastIndex

    Column(
        verticalArrangement = Arrangement.spacedBy(
            dimensions.sm
        )
    ) {
        Text(
            text = stringResource(
                R.string.mash_create_difficulty_title,
                stringResource(difficulty.titleRes),
                difficulty.minSidePx
            ),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
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
                thumbColor = colors.primaryAction,
                activeTrackColor = colors.primaryAction,
                inactiveTrackColor = colors.surfaceSoft,
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
                    color = colors.textSecondary,
                )
            }
        }
    }
}
