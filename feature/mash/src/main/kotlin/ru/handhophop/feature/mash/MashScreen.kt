package ru.handhophop.feature.mash

import android.graphics.Paint
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.floor
import ru.handhophop.core.design.BackgroundPattern
import ru.handhophop.feature.mash.MashCreate.MashCreateConfig
import ru.handhophop.feature.mash.MashCreate.MashCreateData
import ru.handhophop.feature.mash.MashCreate.MashThread

@Composable
internal fun MashScreen(
    viewModel: MashViewModel,
    config: MashCreateConfig,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(config) {
        viewModel.handleAction(GenerateSchemeAction(config))
    }

    CenterContentMash(
        uiState = uiState,
        onDownloadClick = {
            viewModel.handleAction(ClickDownloadsAction())
        },
        onHighlightColorToggle = { paletteIndex ->
            viewModel.handleAction(TogglePaletteHighlightAction(paletteIndex))
        }
    )
}

@Composable
private fun CenterContentMash(
    uiState: MashUiState,
    onDownloadClick: () -> Unit,
    onHighlightColorToggle: (Int) -> Unit,
) {
    val horizontalPadding = dimensionResource(R.dimen.mash_screen_horizontal_padding)
    val verticalPadding = dimensionResource(R.dimen.mash_screen_vertical_padding)
    val contentSpacing = dimensionResource(R.dimen.mash_content_spacing)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.mash_background))
    ) {
        BackgroundPattern()

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                SchemeCard(
                    loading = uiState.isLoading,
                    scheme = uiState.scheme,
                    errorTextRes = uiState.errorTextRes,
                    selectedPaletteIndex = uiState.selectedPaletteIndex,
                    onCellClick = onHighlightColorToggle,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = horizontalPadding,
                            end = horizontalPadding,
                            top = verticalPadding
                        )
                )
            }

            DownloadButton(
                enabled = uiState.isDownloadButtonEnabled,
                onClick = onDownloadClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
            )

            if (uiState.isPaletteVisible) {
                PaletteBar(
                    threads = uiState.visiblePalette,
                    selectedPaletteIndex = uiState.selectedPaletteIndex,
                    onPaletteColorClick = onHighlightColorToggle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = horizontalPadding,
                            end = horizontalPadding,
                            bottom = verticalPadding
                        )
                )
            }
        }
    }
}

@Composable
private fun PaletteBar(
    threads: List<MashThread>,
    selectedPaletteIndex: Int?,
    onPaletteColorClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleSpacing = dimensionResource(R.dimen.mash_palette_title_spacing)
    val rowSpacing = dimensionResource(R.dimen.mash_palette_row_spacing)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(titleSpacing)
    ) {
        Text(
            text = stringResource(R.string.mash_palette_title),
            color = colorResource(R.color.mash_text_primary),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(rowSpacing)
        ) {
            itemsIndexed(
                items = threads,
                key = { index, thread -> "${thread.article}_$index" }
            ) { index, thread ->
                ColorSwatch(
                    thread = thread,
                    number = index + 1,
                    isSelected = selectedPaletteIndex == index,
                    onClick = { onPaletteColorClick(index) }
                )
            }
        }
    }
}

@Composable
private fun SchemeCard(
    loading: Boolean,
    scheme: SchemeData?,
    @StringRes errorTextRes: Int?,
    selectedPaletteIndex: Int?,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cornerRadius = dimensionResource(R.dimen.mash_card_corner_radius)
    val elevation = dimensionResource(R.dimen.mash_card_elevation)
    val contentPadding = dimensionResource(R.dimen.mash_card_content_padding)

    val aspect = scheme?.let { it.gridW.toFloat() / it.gridH.toFloat() } ?: 1f

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.mash_surface)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading -> CircularProgressIndicator(
                    color = colorResource(R.color.mash_primary)
                )

                scheme != null -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspect)
                ) {
                    NumberedSchemeCanvas(
                        scheme = scheme,
                        selectedPaletteIndex = selectedPaletteIndex,
                        onCellClick = onCellClick,
                    )
                }

                errorTextRes != null -> Text(
                    text = stringResource(errorTextRes),
                    color = colorResource(R.color.mash_text_secondary),
                    modifier = Modifier.padding(contentPadding)
                )

                else -> Text(
                    text = stringResource(R.string.mash_no_scheme),
                    color = colorResource(R.color.mash_text_secondary),
                    modifier = Modifier.padding(contentPadding)
                )
            }
        }
    }
}

@Composable
private fun DownloadButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val height = dimensionResource(R.dimen.mash_button_height)
    val cornerRadius = dimensionResource(R.dimen.mash_button_corner_radius)
    val elevation = dimensionResource(R.dimen.mash_button_elevation)

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(cornerRadius),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = elevation),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.mash_primary),
            contentColor = colorResource(R.color.mash_white),
            disabledContainerColor = colorResource(R.color.mash_primary_disabled),
            disabledContentColor = colorResource(R.color.mash_text_secondary)
        )
    ) {
        Text(
            text = stringResource(R.string.mash_download_scheme),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ColorSwatch(
    thread: MashThread,
    number: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val swatchTextSpacing = dimensionResource(R.dimen.mash_swatch_text_spacing)
    val swatchSize = dimensionResource(R.dimen.mash_swatch_size)
    val swatchCornerRadius = dimensionResource(R.dimen.mash_swatch_corner_radius)

    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(swatchTextSpacing)
    ) {
        Box(
            modifier = Modifier
                .size(swatchSize)
                .background(thread.color, RoundedCornerShape(swatchCornerRadius))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) {
                        colorResource(R.color.mash_primary)
                    } else {
                        Color.Transparent
                    },
                    shape = RoundedCornerShape(swatchCornerRadius)
                )
        )

        Text(
            text = number.toString(),
            fontSize = 10.sp,
            color = colorResource(R.color.mash_swatch_text)
        )

        Text(
            text = thread.article,
            style = MaterialTheme.typography.labelSmall,
            color = colorResource(R.color.mash_swatch_text)
        )
    }
}

@Composable
private fun NumberedSchemeCanvas(
    scheme: SchemeData,
    selectedPaletteIndex: Int?,
    onCellClick: (Int) -> Unit,
) {
    var scale by remember(scheme.gridW, scheme.gridH, scheme.indices.size) {
        mutableFloatStateOf(1f)
    }
    var offset by remember(scheme.gridW, scheme.gridH, scheme.indices.size) {
        mutableStateOf(Offset.Zero)
    }

    val paletteNumbers = remember(scheme.palette) {
        List(scheme.palette.size) { index -> (index + 1).toString() }
    }

    val minScale = integerResource(R.integer.mash_min_scale).toFloat()
    val maxScale = integerResource(R.integer.mash_max_scale).toFloat()

    val drawNumbersThresholdPx = with(LocalDensity.current) {
        dimensionResource(R.dimen.mash_scheme_draw_numbers_threshold).toPx()
    }
    val drawGridThresholdPx = with(LocalDensity.current) {
        dimensionResource(R.dimen.mash_scheme_draw_grid_threshold).toPx()
    }
    val tapSlopPx = LocalViewConfiguration.current.touchSlop
    val tapSlopSquared = tapSlopPx * tapSlopPx

    val schemeBackgroundColor = colorResource(R.color.mash_white)
    val gridStrokeColor = colorResource(R.color.mash_grid_stroke)
    val numberTextColor = colorResource(R.color.mash_number_text)

    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    fun clampOffset(
        viewWidth: Float,
        viewHeight: Float,
        currentScale: Float,
        currentOffset: Offset,
    ): Offset {
        val scaledWidth = viewWidth * currentScale
        val scaledHeight = viewHeight * currentScale
        val minX = kotlin.math.min(0f, viewWidth - scaledWidth)
        val minY = kotlin.math.min(0f, viewHeight - scaledHeight)

        return Offset(
            x = currentOffset.x.coerceIn(minX, 0f),
            y = currentOffset.y.coerceIn(minY, 0f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(schemeBackgroundColor)
            .pointerInput(scheme.gridW, scheme.gridH, scheme.indices.size, selectedPaletteIndex) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var tapCandidate = true
                    var active = false
                    var lastPointerPosition = down.position

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.any { it.pressed }
                        val pressedPointers = event.changes.count { it.pressed }

                        if (pressedPointers > 1) {
                            tapCandidate = false
                        }

                        event.changes.forEach { change ->
                            if (change.pressed) {
                                lastPointerPosition = change.position
                            }

                            val dx = change.position.x - down.position.x
                            val dy = change.position.y - down.position.y
                            if ((dx * dx) + (dy * dy) > tapSlopSquared) {
                                tapCandidate = false
                            }
                        }

                        val zoom = event.calculateZoom()
                        if (zoom < 0.999f || zoom > 1.001f) {
                            tapCandidate = false
                        }

                        if (!pressed) {
                            if (tapCandidate) {
                                val viewWidth = size.width.toFloat()
                                val viewHeight = size.height.toFloat()
                                val cell = minOf(
                                    viewWidth / scheme.gridW.toFloat(),
                                    viewHeight / scheme.gridH.toFloat()
                                )

                                val contentX = (lastPointerPosition.x - offset.x) / scale
                                val contentY = (lastPointerPosition.y - offset.y) / scale

                                val column = floor(contentX / cell).toInt()
                                val row = floor(contentY / cell).toInt()

                                if (column in 0 until scheme.gridW && row in 0 until scheme.gridH) {
                                    val paletteIndex = scheme.indices[row * scheme.gridW + column]
                                    onCellClick(paletteIndex)
                                }
                            }
                            break
                        }

                        if (!active) {
                            if (pressedPointers >= 2 || scale > 1f || !tapCandidate) {
                                active = true
                            } else {
                                continue
                            }
                        }

                        val pan = event.calculatePan()
                        val centroid = event.calculateCentroid()

                        val viewWidth = size.width.toFloat()
                        val viewHeight = size.height.toFloat()

                        val oldScale = scale
                        val newScale = (oldScale * zoom).coerceIn(minScale, maxScale)
                        val ratio = newScale / oldScale
                        val newOffset = (offset - centroid) * ratio + centroid + pan

                        scale = newScale
                        offset = clampOffset(viewWidth, viewHeight, newScale, newOffset)

                        event.changes.forEach { change ->
                            if (change.positionChanged()) {
                                change.consume()
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offset.x
                    translationY = offset.y
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0f, 0f)
                }
        ) {
            val width = scheme.gridW
            val height = scheme.gridH
            val cell = minOf(size.width / width.toFloat(), size.height / height.toFloat())
            val cellSize = Size(cell, cell)

            val left = (-offset.x) / scale
            val top = (-offset.y) / scale
            val right = (size.width - offset.x) / scale
            val bottom = (size.height - offset.y) / scale

            val x0 = floor(left / cell).toInt().coerceIn(0, width - 1)
            val y0 = floor(top / cell).toInt().coerceIn(0, height - 1)
            val x1 = ceil(right / cell).toInt().coerceIn(0, width - 1)
            val y1 = ceil(bottom / cell).toInt().coerceIn(0, height - 1)

            val effectiveCellPx = cell * scale
            val drawNumbers = effectiveCellPx >= drawNumbersThresholdPx
            val drawGrid = effectiveCellPx >= drawGridThresholdPx
            val gridStroke = androidx.compose.ui.graphics.drawscope.Stroke(
                width = (1f / scale).coerceAtLeast(0.5f)
            )

            val highlightedIndex = selectedPaletteIndex ?: -1
            val hasHighlight = highlightedIndex >= 0

            for (yy in y0..y1) {
                val topY = yy * cell

                for (xx in x0..x1) {
                    val index = yy * width + xx
                    val paletteIndex = scheme.indices[index]
                    val thread = scheme.palette[paletteIndex]
                    val isHighlighted = !hasHighlight || paletteIndex == highlightedIndex

                    val leftX = xx * cell
                    val topLeft = Offset(leftX, topY)

                    if (isHighlighted) {
                        drawRect(
                            color = thread.color.copy(alpha = if (hasHighlight) 0.42f else 0.18f),
                            topLeft = topLeft,
                            size = cellSize
                        )
                    } else {
                        drawRect(
                            color = schemeBackgroundColor,
                            topLeft = topLeft,
                            size = cellSize
                        )
                    }

                    if (drawGrid) {
                        drawRect(
                            color = gridStrokeColor,
                            topLeft = topLeft,
                            size = cellSize,
                            style = gridStroke
                        )
                    }

                    if (drawNumbers && isHighlighted) {
                        textPaint.color = numberTextColor.toArgb()
                        textPaint.textSize = cell * 0.55f

                        val cx = leftX + cell / 2f
                        val cy = topY + cell / 2f -
                                (textPaint.ascent() + textPaint.descent()) / 2f

                        drawContext.canvas.nativeCanvas.drawText(
                            paletteNumbers[paletteIndex],
                            cx,
                            cy,
                            textPaint
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MashScreenPreview() {
    MaterialTheme {
        CenterContentMash(
            uiState = previewMashUiState(),
            onDownloadClick = {},
            onHighlightColorToggle = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MashScreenHighlightedPreview() {
    MaterialTheme {
        CenterContentMash(
            uiState = previewMashUiState(selectedPaletteIndex = 2),
            onDownloadClick = {},
            onHighlightColorToggle = {},
        )
    }
}

private fun previewMashUiState(
    selectedPaletteIndex: Int? = null,
): MashUiState {
    val threads = MashCreateData.getThreadsByCount(10).take(6)
    val scheme = previewSchemeData(threads)

    return MashUiState(
        isLoading = false,
        scheme = scheme,
        visiblePalette = threads,
        errorTextRes = null,
        isDownloadButtonEnabled = true,
        isPaletteVisible = true,
        selectedPaletteIndex = selectedPaletteIndex,
    )
}

private fun previewSchemeData(
    threads: List<MashThread>,
): SchemeData {
    val gridWidth = 18
    val gridHeight = 18

    val indices = IntArray(gridWidth * gridHeight) { index ->
        val row = index / gridWidth
        val column = index % gridWidth
        ((row / 2) + (column / 3)) % threads.size
    }

    return SchemeData(
        gridW = gridWidth,
        gridH = gridHeight,
        palette = threads,
        indices = indices,
    )
}