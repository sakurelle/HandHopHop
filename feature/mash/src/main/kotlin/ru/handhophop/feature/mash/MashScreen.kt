package ru.handhophop.feature.mash

import android.graphics.Paint
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import ru.handhophop.core.design.BackgroundPattern
import ru.handhophop.core.design.ButtonState
import ru.handhophop.core.design.HandHopHopButton
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.core.design.TopBar
import ru.handhophop.core.design.TopBarState
import ru.handhophop.feature.mash.MashCreate.MashThread

private const val SCHEME_DEFAULT_FILL_ALPHA = 0.18f
private const val SCHEME_SELECTED_FILL_ALPHA = 0.42f
private const val SWATCH_LIGHT_LUMINANCE_THRESHOLD = 0.65f
private const val SCHEME_NUMBER_DARK_TEXT_THRESHOLD = 0.6f
private const val SCHEME_MAJOR_GRID_STEP = 10
private const val MASH_MAX_SCALE = 4f

@Composable
internal fun MashScreen(
    title: String,
    uiState: MashUiState,
    onBackClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onSchemeCellClick: (Int) -> Unit,
    onHighlightColorToggle: (Int) -> Unit,
    onPaletteCompletionToggle: (Int) -> Unit,
    onClearSelection: () -> Unit,
) {
    CenterContentMash(
        title = title,
        uiState = uiState,
        onBackClick = onBackClick,
        onDownloadClick = onDownloadClick,
        onSchemeCellClick = onSchemeCellClick,
        onHighlightColorToggle = onHighlightColorToggle,
        onPaletteCompletionToggle = onPaletteCompletionToggle,
        onClearSelection = onClearSelection,
    )
}

@Composable
private fun CenterContentMash(
    title: String,
    uiState: MashUiState,
    onBackClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onSchemeCellClick: (Int) -> Unit,
    onHighlightColorToggle: (Int) -> Unit,
    onPaletteCompletionToggle: (Int) -> Unit,
    onClearSelection: () -> Unit,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val horizontalPadding = dimensions.md
    val verticalPadding = dimensions.md
    val contentSpacing = dimensions.md

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        BackgroundPattern()

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            TopBar(
                state = TopBarState(
                    projectName = title.ifBlank {
                        stringResource(R.string.mash_workspace_title_fallback)
                    },
                    leftIconRes = R.drawable.arrow,
                    titleRes = null
                ),
                onClickLeft = onBackClick,
                onClickRight = {Unit}

            )

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
                    completedCellIndices = uiState.completedCellIndices,
                    onCellClick = onSchemeCellClick,
                    onBackgroundClick = onClearSelection,
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
                    onPaletteColorLongClick = onPaletteCompletionToggle,
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
    onPaletteColorLongClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions = HandHopHopDesignSystem.dimensions
    val rowSpacing = dimensions.sm
    val horizontalPadding = dimensions.sm

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = horizontalPadding),
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
                isCompleted = thread.isCompleted,
                onClick = { onPaletteColorClick(index) },
                onLongClick = { onPaletteColorLongClick(index) },
            )
        }
    }
}

@Composable
private fun SchemeCard(
    loading: Boolean,
    scheme: SchemeData?,
    @StringRes errorTextRes: Int?,
    selectedPaletteIndex: Int?,
    completedCellIndices: Set<Int>,
    onCellClick: (Int) -> Unit,
    onBackgroundClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val contentPadding = dimensions.md

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(dimensions.lg),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.xs)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading -> CircularProgressIndicator(
                    color = colors.primaryAction
                )

                scheme != null -> SchemeWorkspace(
                    scheme = scheme,
                    selectedPaletteIndex = selectedPaletteIndex,
                    completedCellIndices = completedCellIndices,
                    onCellClick = onCellClick,
                    onBackgroundClick = onBackgroundClick,
                )

                errorTextRes != null -> Text(
                    text = stringResource(errorTextRes),
                    color = colors.error,
                    modifier = Modifier.padding(contentPadding)
                )

                else -> Text(
                    text = stringResource(R.string.mash_no_scheme),
                    color = colors.textSecondary,
                    modifier = Modifier.padding(contentPadding)
                )
            }
        }
    }
}

@Composable
private fun SchemeWorkspace(
    scheme: SchemeData,
    selectedPaletteIndex: Int?,
    completedCellIndices: Set<Int>,
    onCellClick: (Int) -> Unit,
    onBackgroundClick: () -> Unit,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val workspaceShape = RoundedCornerShape(dimensions.md)
    val workspacePadding = dimensions.sm
    val workspaceBorderWidth = dimensions.xs / 4

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(workspacePadding)
            .clip(workspaceShape)
            .background(colors.notWhite)
            .border(
                width = workspaceBorderWidth,
                color = colors.textPrimary,
                shape = workspaceShape,
            )
    ) {
        NumberedSchemeCanvas(
            scheme = scheme,
            selectedPaletteIndex = selectedPaletteIndex,
            completedCellIndices = completedCellIndices,
            onCellClick = onCellClick,
            onBackgroundClick = onBackgroundClick,
        )
    }
}

@Composable
private fun DownloadButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions

    HandHopHopButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        size = ButtonState.Size.FILL,
        textColor = ButtonState.Color.White,
        buttonColor = ButtonState.Color.Button,
    ) {
        Text(
            text = stringResource(R.string.mash_download_scheme),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColorSwatch(
    thread: MashThread,
    number: Int,
    isSelected: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val swatchTextSpacing = dimensions.xs
    val swatchSize = dimensions.xl
    val swatchCornerRadius = dimensions.xs
    val swatchBorderWidth = dimensions.xs / 4
    val swatchSelectedBorderWidth = dimensions.xs / 2
    val swatchSelectedPadding = dimensions.xs / 2
    val bottomSpacing = 0.dp

    val innerShape = RoundedCornerShape(swatchCornerRadius)
    val outerShape = RoundedCornerShape(swatchCornerRadius + swatchSelectedPadding)
    val selectedBorderColor = colors.primaryAction
    val completedBorderColor = colors.textPrimary
    val articleColor = colors.textPrimary.copy(alpha = 0.72f)
    val numberColor = if (thread.color.luminance() > SWATCH_LIGHT_LUMINANCE_THRESHOLD) {
        colors.textPrimary
    } else {
        colors.notWhite
    }

    Column(
        modifier = Modifier.widthIn(min = swatchSize),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(swatchTextSpacing)
    ) {
        Box(
            modifier = Modifier
                .clip(outerShape)
                .border(
                    width = if (isSelected) swatchSelectedBorderWidth else 0.dp,
                    color = if (isSelected) selectedBorderColor else Color.Transparent,
                    shape = outerShape,
                )
                .padding(swatchSelectedPadding)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
        ) {
            Box(
                modifier = Modifier
                    .size(swatchSize)
                    .clip(innerShape)
                    .background(thread.color)
                    .border(
                        width = swatchBorderWidth,
                        color = completedBorderColor,
                        shape = innerShape,
                    )
                    .padding(bottomSpacing),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.SemiBold,
                    color = numberColor,
                )
            }
        }

        Text(
            text = thread.article,
            style = MaterialTheme.typography.labelSmall,
            color = articleColor,
        )
    }
}

@Composable
private fun NumberedSchemeCanvas(
    scheme: SchemeData,
    selectedPaletteIndex: Int?,
    completedCellIndices: Set<Int>,
    onCellClick: (Int) -> Unit,
    onBackgroundClick: () -> Unit,
) {
    val colors = HandHopHopDesignSystem.colors
    val dimensions = HandHopHopDesignSystem.dimensions
    val tapSlopPx = LocalViewConfiguration.current.touchSlop
    val tapSlopSquared = tapSlopPx * tapSlopPx
    val drawNumbersThresholdPx = with(LocalDensity.current) { dimensions.sm.toPx() }

    val schemeBackgroundColor = colors.notWhite
    val gridStrokeColor = colors.primaryAction.copy(alpha = 0.40f)
    val majorGridStrokeColor = colors.textPrimary.copy(alpha = 0.70f)
    val numberTextColor = colors.textPrimary.copy(alpha = 0.80f)

    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    var scale by remember(scheme.gridW, scheme.gridH, scheme.indices.size) {
        mutableFloatStateOf(1f)
    }
    var canvasSize by remember(scheme.gridW, scheme.gridH, scheme.indices.size) {
        mutableStateOf(IntSize.Zero)
    }
    var offset by remember(scheme.gridW, scheme.gridH, scheme.indices.size) {
        mutableStateOf<Offset?>(null)
    }

    val paletteNumbers = remember(scheme.palette) {
        List(scheme.palette.size) { index -> (index + 1).toString() }
    }

    fun baseCell(viewWidth: Float, viewHeight: Float): Float {
        return max(
            viewWidth / scheme.gridW.toFloat(),
            viewHeight / scheme.gridH.toFloat()
        )
    }

    fun centeredOffset(viewWidth: Float, viewHeight: Float, currentScale: Float): Offset {
        val cell = baseCell(viewWidth, viewHeight) * currentScale
        val contentWidth = scheme.gridW * cell
        val contentHeight = scheme.gridH * cell
        return Offset(
            x = (viewWidth - contentWidth) / 2f,
            y = (viewHeight - contentHeight) / 2f,
        )
    }

    fun clampOffset(
        viewWidth: Float,
        viewHeight: Float,
        currentScale: Float,
        currentOffset: Offset,
    ): Offset {
        val cell = baseCell(viewWidth, viewHeight) * currentScale
        val contentWidth = scheme.gridW * cell
        val contentHeight = scheme.gridH * cell

        val clampedX = if (contentWidth <= viewWidth) {
            (viewWidth - contentWidth) / 2f
        } else {
            currentOffset.x.coerceIn(viewWidth - contentWidth, 0f)
        }

        val clampedY = if (contentHeight <= viewHeight) {
            (viewHeight - contentHeight) / 2f
        } else {
            currentOffset.y.coerceIn(viewHeight - contentHeight, 0f)
        }

        return Offset(clampedX, clampedY)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(schemeBackgroundColor)
            .onSizeChanged { size ->
                canvasSize = size
                offset = clampOffset(
                    viewWidth = size.width.toFloat(),
                    viewHeight = size.height.toFloat(),
                    currentScale = scale,
                    currentOffset = offset ?: centeredOffset(
                        size.width.toFloat(),
                        size.height.toFloat(),
                        scale,
                    ),
                )
            }
            .pointerInput(
                scheme.gridW,
                scheme.gridH,
                scheme.indices.size,
                selectedPaletteIndex,
                completedCellIndices,
            ) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var tapCandidate = true
                    var active = false
                    var lastPointerPosition = down.position

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.any { it.pressed }
                        val pressedPointers = event.changes.count { it.pressed }
                        val currentOffset = offset ?: centeredOffset(size.width.toFloat(), size.height.toFloat(), scale)

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
                                val cell = baseCell(size.width.toFloat(), size.height.toFloat()) * scale
                                val column = floor((lastPointerPosition.x - currentOffset.x) / cell).toInt()
                                val row = floor((lastPointerPosition.y - currentOffset.y) / cell).toInt()

                                if (column in 0 until scheme.gridW && row in 0 until scheme.gridH) {
                                    val cellIndex = row * scheme.gridW + column
                                    onCellClick(cellIndex)
                                } else {
                                    onBackgroundClick()
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
                        val newScale = (oldScale * zoom).coerceIn(1f, MASH_MAX_SCALE)

                        val oldCell = baseCell(viewWidth, viewHeight) * oldScale
                        val newCell = baseCell(viewWidth, viewHeight) * newScale

                        val contentX = (centroid.x - currentOffset.x) / oldCell
                        val contentY = (centroid.y - currentOffset.y) / oldCell

                        val newOffset = Offset(
                            x = centroid.x - (contentX * newCell) + pan.x,
                            y = centroid.y - (contentY * newCell) + pan.y,
                        )

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
            modifier = Modifier.fillMaxSize()
        ) {
            val resolvedOffset = offset ?: centeredOffset(size.width, size.height, scale)
            val cell = baseCell(size.width, size.height) * scale
            val cellSize = Size(cell, cell)

            val left = (-resolvedOffset.x) / cell
            val top = (-resolvedOffset.y) / cell
            val right = (size.width - resolvedOffset.x) / cell
            val bottom = (size.height - resolvedOffset.y) / cell

            val x0 = floor(left).toInt().coerceIn(0, scheme.gridW - 1)
            val y0 = floor(top).toInt().coerceIn(0, scheme.gridH - 1)
            val x1 = ceil(right).toInt().coerceIn(0, scheme.gridW - 1)
            val y1 = ceil(bottom).toInt().coerceIn(0, scheme.gridH - 1)

            val drawNumbers = cell >= drawNumbersThresholdPx
            val gridStroke = androidx.compose.ui.graphics.drawscope.Stroke(
                width = (0.8f / scale).coerceAtLeast(0.25f)
            )
            val majorGridStroke = androidx.compose.ui.graphics.drawscope.Stroke(
                width = (1.8f / scale).coerceAtLeast(0.5f)
            )

            for (yy in y0..y1) {
                for (xx in x0..x1) {
                    val index = yy * scheme.gridW + xx
                    val paletteIndex = scheme.indices[index]
                    val thread = scheme.palette[paletteIndex]
                    val isCompleted = index in completedCellIndices
                    val isSelected = selectedPaletteIndex == paletteIndex

                    val leftX = resolvedOffset.x + (xx * cell)
                    val topY = resolvedOffset.y + (yy * cell)
                    val topLeft = Offset(leftX, topY)

                    val fillColor = when {
                        isCompleted -> thread.color
                        selectedPaletteIndex == null -> thread.color.copy(alpha = SCHEME_DEFAULT_FILL_ALPHA)
                        isSelected -> thread.color.copy(alpha = SCHEME_SELECTED_FILL_ALPHA)
                        else -> schemeBackgroundColor
                    }

                    drawRect(
                        color = fillColor,
                        topLeft = topLeft,
                        size = cellSize
                    )

                    drawRect(
                        color = gridStrokeColor,
                        topLeft = topLeft,
                        size = cellSize,
                        style = gridStroke
                    )

                    val shouldDrawNumber = !isCompleted && (drawNumbers || isSelected)

                    if (shouldDrawNumber) {
                        val cellNumberColor = when {
                            isSelected -> if (thread.color.luminance() > SCHEME_NUMBER_DARK_TEXT_THRESHOLD) {
                                numberTextColor
                            } else {
                                schemeBackgroundColor
                            }

                            else -> numberTextColor
                        }

                        textPaint.color = cellNumberColor.toArgb()
                        textPaint.textSize = cell * 0.5f

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

            val verticalStart = (x0 / SCHEME_MAJOR_GRID_STEP) * SCHEME_MAJOR_GRID_STEP
            val verticalEnd = minOf(scheme.gridW, x1 + 1)
            var majorX = verticalStart
            while (majorX <= verticalEnd) {
                val lineX = resolvedOffset.x + (majorX * cell)
                drawLine(
                    color = majorGridStrokeColor,
                    start = Offset(lineX, resolvedOffset.y + (y0 * cell)),
                    end = Offset(lineX, resolvedOffset.y + ((y1 + 1) * cell)),
                    strokeWidth = majorGridStroke.width,
                )
                majorX += SCHEME_MAJOR_GRID_STEP
            }

            val horizontalStart = (y0 / SCHEME_MAJOR_GRID_STEP) * SCHEME_MAJOR_GRID_STEP
            val horizontalEnd = minOf(scheme.gridH, y1 + 1)
            var majorY = horizontalStart
            while (majorY <= horizontalEnd) {
                val lineY = resolvedOffset.y + (majorY * cell)
                drawLine(
                    color = majorGridStrokeColor,
                    start = Offset(resolvedOffset.x + (x0 * cell), lineY),
                    end = Offset(resolvedOffset.x + ((x1 + 1) * cell), lineY),
                    strokeWidth = majorGridStroke.width,
                )
                majorY += SCHEME_MAJOR_GRID_STEP
            }
        }
    }
}
