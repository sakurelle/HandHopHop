package ru.handhophop.feature.mash.MashCreate

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalContext
import kotlin.math.roundToInt
import ru.handhophop.core.design.ButtonState
import ru.handhophop.core.design.HandHopHopButton
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.core.design.TopBar
import ru.handhophop.core.design.TopBarState
import ru.handhophop.core.session.PremiumProvider
import ru.handhophop.feature.mash.R
import ru.handhophop.feature.mash.byteArrayToBitmap
import ru.handhophop.feature.mash.loadBitmapFromUrl
import ru.handhophop.feature.mash.readImageBytesFromFile
import ru.handhophop.feature.mash.selectPaletteForImage

private sealed interface ImagePreviewState {
    data object Empty : ImagePreviewState
    data object Loading : ImagePreviewState
    data object Failed : ImagePreviewState

    data class Loaded(
        val bitmap: Bitmap,
    ) : ImagePreviewState
}

@Composable
internal fun MashCreateScreen(
    imageUrl: String?,
    localImagePath: String? = null,
    localImageBytes: ByteArray? = null,
    imageLoadFailed: Boolean = false,
    suggestedProjectName: String = "",
    onCreateFinished: (MashCreateConfig) -> Unit = {},
    onBackClick: () -> Unit = {},
    onPickLocalImage: () -> Unit = {},
    onOpenFeed: () -> Unit = {},
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
    val hasLocalImage = !localImagePath.isNullOrBlank() ||
            localImageBytes?.isNotEmpty() == true
    val hasOnlineImage = !imageUrl.isNullOrBlank()
    val previewState by produceState(
        initialValue = if (hasLocalImage || hasOnlineImage) {
            ImagePreviewState.Loading
        } else {
            ImagePreviewState.Empty
        },
        key1 = imageUrl,
        key2 = localImagePath,
        key3 = localImageBytes,
    ) {
        value = when {
            localImageBytes?.isNotEmpty() == true -> {
                byteArrayToBitmap(localImageBytes)
                    ?.let(ImagePreviewState::Loaded)
                    ?: ImagePreviewState.Failed
            }

            !localImagePath.isNullOrBlank() -> {
                readImageBytesFromFile(localImagePath)
                    ?.let(::byteArrayToBitmap)
                    ?.let(ImagePreviewState::Loaded)
                    ?: ImagePreviewState.Failed
            }

            !imageUrl.isNullOrBlank() -> {
                loadBitmapFromUrl(
                    context = context,
                    url = imageUrl,
                )
                    ?.let(ImagePreviewState::Loaded)
                    ?: ImagePreviewState.Failed
            }

            else -> ImagePreviewState.Empty
        }
    }

    val previewBitmap = (previewState as? ImagePreviewState.Loaded)?.bitmap
    val previewThreads = previewBitmap?.let { bitmap ->
        selectPaletteForImage(
            source = bitmap,
            minSideCells = difficulty.minSidePx,
            availablePalette = MashCreateData.allThreads,
            maxColors = colorCount,
        )
    } ?: MashCreateData.getThreadsByCount(colorCount)
    val isCreateButtonEnabled = projectName.isNotBlank() &&
            previewState is ImagePreviewState.Loaded

    fun createWork() {
        if (!isCreateButtonEnabled) return

        onCreateFinished(
            MashCreateConfig(
                projectName = projectName.trim(),
                imageUrl = if (hasLocalImage) null else imageUrl,
                imagePath = localImagePath?.takeIf { hasLocalImage },
                schemeType = MashCreateSchemeType.EMBROIDERY,
                colorCount = colorCount,
                difficulty = difficulty,
                threads = previewThreads,
            )
        )
    }

    MashCreateContent(
        projectName = projectName,
        previewState = previewState,
        imageSourceLabelRes = when {
            hasLocalImage -> R.string.mash_create_image_source_local
            hasOnlineImage -> R.string.mash_create_image_source_online
            else -> null
        },
        imageLoadFailed = imageLoadFailed || previewState is ImagePreviewState.Failed,
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
        onPickLocalImage = onPickLocalImage,
        onOpenFeed = onOpenFeed,
    )
}

@Composable
private fun MashCreateContent(
    projectName: String,
    previewState: ImagePreviewState,
    imageSourceLabelRes: Int?,
    imageLoadFailed: Boolean,
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
    onPickLocalImage: () -> Unit,
    onOpenFeed: () -> Unit,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopBar(
                state = TopBarState(
                    titleRes = R.string.mash_create_screen_title,
                    leftIconRes = R.drawable.arrow,
                    rightIconRes = null,
                ),
                onClickLeft = onBackClick,
                onClickRight = { },
            )
        },
        containerColor = Color.Transparent,
        contentColor = colors.textPrimary,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                    shape = RoundedCornerShape(dimensions.lg),
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

                        MashCreateImageSection(
                            previewState = previewState,
                            imageSourceLabelRes = imageSourceLabelRes,
                            imageLoadFailed = imageLoadFailed,
                            onPickLocalImage = onPickLocalImage,
                            onOpenFeed = onOpenFeed,
                        )

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

                        HandHopHopButton(
                            onClick = onCreateClick,
                            enabled = isCreateButtonEnabled,
                            size = ButtonState.Size.FILL,
                            textColor = ButtonState.Color.White,
                            buttonColor = ButtonState.Color.Button,
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
            shape = RoundedCornerShape(dimensions.md),
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
    previewState: ImagePreviewState,
    imageSourceLabelRes: Int?,
    imageLoadFailed: Boolean,
    onPickLocalImage: () -> Unit,
    onOpenFeed: () -> Unit,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val imageShape = RoundedCornerShape(dimensions.md)
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

        if (imageSourceLabelRes != null) {
            Text(
                text = stringResource(imageSourceLabelRes),
                style = MaterialTheme.typography.labelMedium,
                color = colors.primaryAction,
                modifier = Modifier
                    .clip(RoundedCornerShape(dimensions.sm))
                    .background(colors.surfaceSoft)
                    .border(
                        width = dimensions.xs / 4,
                        color = colors.primaryAction,
                        shape = RoundedCornerShape(dimensions.sm),
                    )
                    .padding(
                        horizontal = dimensions.sm,
                        vertical = dimensions.xs,
                    ),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(imageShape)
                .background(colors.surfaceSoft)
                .border(
                    width = dimensions.xs / 4,
                    color = colors.outline,
                    shape = imageShape
            ),
            contentAlignment = Alignment.Center,
        ) {
            when (previewState) {
                ImagePreviewState.Empty -> {
                    ImagePlaceholderText(
                        text = stringResource(R.string.mash_create_image_placeholder),
                    )
                }

                ImagePreviewState.Loading -> {
                    ImagePlaceholderText(
                        text = stringResource(R.string.mash_create_image_selected),
                    )
                }

                ImagePreviewState.Failed -> {
                    ImagePlaceholderText(
                        text = stringResource(R.string.mash_create_image_load_failed),
                    )
                }

                is ImagePreviewState.Loaded -> {
                    val previewBitmap = previewState.bitmap
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

        if (imageLoadFailed) {
            Text(
                text = stringResource(R.string.mash_create_image_load_failed),
                color = colors.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(dimensions.sm),
        ) {
            HandHopHopButton(
                onClick = onPickLocalImage,
                size = ButtonState.Size.FILL,
                textColor = ButtonState.Color.Button,
                buttonColor = ButtonState.Color.Background,
            ) {
                Text(
                    text = stringResource(R.string.mash_create_choose_from_gallery),
                    color = colors.primaryAction,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            HandHopHopButton(
                onClick = onOpenFeed,
                size = ButtonState.Size.FILL,
                textColor = ButtonState.Color.White,
                buttonColor = ButtonState.Color.Button,
            ) {
                Text(
                    text = stringResource(R.string.mash_create_open_online_gallery),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ImagePlaceholderText(
    text: String,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions

    Text(
        text = text,
        color = colors.textSecondary,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .padding(dimensions.lg),
    )
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
    val shape = RoundedCornerShape(dimensions.sm)
    val backgroundColor = when {
        isSelected -> colors.primaryAction
        isEnabled -> colors.surfaceSoft
        else -> colors.primaryAction.copy(alpha = 0.55f)
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
                width = dimensions.xs / 4,
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
                .size(dimensions.lg)
                .background(thread.color, CircleShape)
                .border(
                    width = dimensions.xs / 4,
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

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun MashCreateDifficultySection(
    difficulty: MashCreateDifficulty,
    onDifficultyChanged: (MashCreateDifficulty) -> Unit,
) {
    val context = LocalContext.current
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val difficultySteps = MashCreateDifficulty.entries.size - 2
    val difficultyLastIndex = MashCreateDifficulty.entries.lastIndex
    val isPremium = PremiumProvider.isPremium()

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
        Box(
            modifier = Modifier
                .wrapContentSize(),
            contentAlignment = Alignment.CenterEnd
        )
        {
            Slider(
                value = difficulty.ordinal.toFloat(),
                onValueChange = { value ->
                    val newIndex = value.roundToInt()
                    if (!isPremium && newIndex == difficultyLastIndex) {
                        onDifficultyChanged(MashCreateDifficulty.entries[difficultyLastIndex - 1])
                    } else {
                        onDifficultyChanged(
                            MashCreateDifficulty.entries[newIndex.coerceIn(
                                0,
                                difficultyLastIndex
                            )]
                        )
                    }
                },
                valueRange = 0f..difficultyLastIndex.toFloat(),
                steps = difficultySteps,
                colors = SliderDefaults.colors(
                    thumbColor = colors.primaryAction,
                    activeTrackColor = colors.primaryAction,
                    inactiveTrackColor = colors.surfaceSoft,
                )
            )
            if (!isPremium) {
                androidx.compose.material3.Icon(
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            android.widget.Toast.makeText(
                                context,
                                context.getString(R.string.premium_required_toast), // Добавьте эту строку в strings.xml
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_lock),
                    contentDescription = "Locked",
                    tint = colors.textSecondary,
                    )
            }
        }
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
