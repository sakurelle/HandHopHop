package ru.handhophop.feature.mash

import android.graphics.Paint
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.floor
import ru.handhophop.feature.mash.MashCreate.MashCreateConfig
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
        }
    )
}

@Composable
private fun CenterContentMash(
    uiState: MashUiState,
    onDownloadClick: () -> Unit,
) {
    val horizontalPadding = dimensionResource(R.dimen.mash_screen_horizontal_padding)
    val verticalPadding = dimensionResource(R.dimen.mash_screen_vertical_padding)
    val contentSpacing = dimensionResource(R.dimen.mash_content_spacing)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.mash_background)),
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

@Composable
private fun PaletteBar(
    threads: List<MashThread>,
    modifier: Modifier = Modifier
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
            itemsIndexed(threads) { index, thread ->
                ColorSwatch(
                    thread = thread,
                    number = index + 1
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
    modifier: Modifier = Modifier
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
                    NumberedSchemeCanvas(scheme = scheme)
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
    modifier: Modifier = Modifier
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
    number: Int
) {
    val swatchTextSpacing = dimensionResource(R.dimen.mash_swatch_text_spacing)
    val swatchSize = dimensionResource(R.dimen.mash_swatch_size)
    val swatchCornerRadius = dimensionResource(R.dimen.mash_swatch_corner_radius)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(swatchTextSpacing)
    ) {
        Box(
            modifier = Modifier
                .size(swatchSize)
                .background(thread.color, RoundedCornerShape(swatchCornerRadius))
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
private fun NumberedSchemeCanvas(scheme: SchemeData) {
    var scale by remember(scheme.gridW, scheme.gridH, scheme.indices.size) {
        mutableFloatStateOf(1f)
    }
    var offset by remember(scheme.gridW, scheme.gridH, scheme.indices.size) {
        mutableStateOf(Offset.Zero)
    }

    val minScale = integerResource(R.integer.mash_min_scale).toFloat()
    val maxScale = integerResource(R.integer.mash_max_scale).toFloat()

    val drawNumbersThresholdPx = with(LocalDensity.current) {
        dimensionResource(R.dimen.mash_scheme_draw_numbers_threshold).toPx()
    }
    val drawGridThresholdPx = with(LocalDensity.current) {
        dimensionResource(R.dimen.mash_scheme_draw_grid_threshold).toPx()
    }

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
            .pointerInput(scheme.gridW, scheme.gridH, scheme.indices.size) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)

                    var active = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.any { it.pressed }
                        if (!pressed) break

                        val pointers = event.changes.count { it.pressed }

                        if (!active) {
                            if (pointers >= 2 || scale > 1f) {
                                active = true
                            } else {
                                continue
                            }
                        }

                        val zoom = event.calculateZoom()
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

            for (yy in y0..y1) {
                val topY = yy * cell

                for (xx in x0..x1) {
                    val index = yy * width + xx
                    val paletteIndex = scheme.indices[index]
                    val thread = scheme.palette[paletteIndex]
                    val color = thread.color

                    val leftX = xx * cell
                    val rectSize = Size(cell, cell)

                    drawRect(
                        color = color.copy(alpha = 0.18f),
                        topLeft = Offset(leftX, topY),
                        size = rectSize
                    )

                    if (drawGrid) {
                        drawRect(
                            color = gridStrokeColor,
                            topLeft = Offset(leftX, topY),
                            size = rectSize,
                            style = gridStroke
                        )
                    }

                    if (drawNumbers) {
                        val number = (paletteIndex + 1).toString()
                        textPaint.color = numberTextColor.toArgb()
                        textPaint.textSize = cell * 0.55f

                        val cx = leftX + cell / 2f
                        val cy = topY + cell / 2f -
                                (textPaint.ascent() + textPaint.descent()) / 2f

                        drawContext.canvas.nativeCanvas.drawText(
                            number,
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