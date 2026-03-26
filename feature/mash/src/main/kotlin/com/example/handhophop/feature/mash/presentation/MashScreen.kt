package com.example.handhophop.feature.mash.presentation

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.handhophop.feature.mash.R
import kotlin.math.ceil
import kotlin.math.floor

@Composable
internal fun MashScreen(
    viewModel: MashViewModel,
    imageUrl: String?
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(imageUrl) {
        viewModel.handleAction(GenerateSchemeAction(imageUrl))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CenterContentMash(
            uiState = uiState,
            onDownloadClick = {
                viewModel.handleAction(ClickDownloadsAction())
            }
        )
    }
}

@Composable
private fun CenterContentMash(
    uiState: MashUiState,
    onDownloadClick: () -> Unit
) {
    val horizontalPadding = dimensionResource(R.dimen.mash_screen_horizontal_padding)
    val verticalPadding = dimensionResource(R.dimen.mash_screen_vertical_padding)
    val contentSpacing = dimensionResource(R.dimen.mash_content_spacing)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalArrangement = Arrangement.spacedBy(contentSpacing)
    ) {
        SchemeCard(
            loading = uiState.isLoading,
            scheme = uiState.scheme,
            error = uiState.error
        )

        DownloadButton(
            enabled = uiState.scheme != null && !uiState.isLoading,
            onClick = onDownloadClick
        )

        if (uiState.visiblePalette.isNotEmpty()) {
            PaletteBar(colors = uiState.visiblePalette)
        }
    }
}

@Composable
private fun PaletteBar(colors: List<Color>) {
    val titleSpacing = dimensionResource(R.dimen.mash_palette_title_spacing)
    val horizontalPadding = dimensionResource(R.dimen.mash_palette_horizontal_padding)
    val rowSpacing = dimensionResource(R.dimen.mash_palette_row_spacing)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(titleSpacing)
    ) {
        Text(
            text = stringResource(R.string.mash_palette_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(rowSpacing)
        ) {
            colors.forEachIndexed { index, color ->
                ColorSwatch(
                    color = color,
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
    error: String?
) {
    val cornerRadius = dimensionResource(R.dimen.mash_card_corner_radius)
    val elevation = dimensionResource(R.dimen.mash_card_elevation)
    val contentPadding = dimensionResource(R.dimen.mash_card_content_padding)

    val aspect = scheme?.let { it.gridW.toFloat() / it.gridH.toFloat() } ?: 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspect),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading -> CircularProgressIndicator()
                scheme != null -> NumberedSchemeCanvas(scheme = scheme)
                else -> Text(
                    text = error ?: stringResource(R.string.mash_no_scheme),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(contentPadding)
                )
            }
        }
    }
}

@Composable
private fun DownloadButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val height = dimensionResource(R.dimen.mash_button_height)
    val cornerRadius = dimensionResource(R.dimen.mash_button_corner_radius)
    val elevation = dimensionResource(R.dimen.mash_button_elevation)

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(cornerRadius),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = elevation),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.mash_download_button_container),
            contentColor = colorResource(R.color.mash_download_button_content),
            disabledContainerColor = colorResource(R.color.mash_download_button_disabled_container),
            disabledContentColor = colorResource(R.color.mash_download_button_disabled_content)
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
    color: Color,
    number: Int
) {
    val swatchTextSpacing = dimensionResource(R.dimen.mash_swatch_text_spacing)
    val swatchSize = dimensionResource(R.dimen.mash_swatch_size)
    val swatchCornerRadius = dimensionResource(R.dimen.mash_swatch_corner_radius)
    val swatchTextSize = dimensionResource(R.dimen.mash_swatch_text_size)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(swatchTextSpacing)
    ) {
        Box(
            modifier = Modifier
                .size(swatchSize)
                .background(color, RoundedCornerShape(swatchCornerRadius))
        )

        Text(
            text = number.toString(),
            fontSize = swatchTextSize.value.sp,
            color = Color.Black.copy(alpha = 0.7f)
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

    val minScale = 1f
    val maxScale = 6f

    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    fun clampOffset(viewW: Float, viewH: Float, sc: Float, off: Offset): Offset {
        val scaledW = viewW * sc
        val scaledH = viewH * sc
        val minX = kotlin.math.min(0f, viewW - scaledW)
        val minY = kotlin.math.min(0f, viewH - scaledH)

        return Offset(
            x = off.x.coerceIn(minX, 0f),
            y = off.y.coerceIn(minY, 0f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color.White)
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

                        val viewW = size.width.toFloat()
                        val viewH = size.height.toFloat()

                        val oldScale = scale
                        val newScale = (oldScale * zoom).coerceIn(minScale, maxScale)
                        val ratio = newScale / oldScale

                        val newOffset = (offset - centroid) * ratio + centroid + pan

                        scale = newScale
                        offset = clampOffset(viewW, viewH, newScale, newOffset)

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
            val w = scheme.gridW
            val h = scheme.gridH
            val cell = minOf(size.width / w.toFloat(), size.height / h.toFloat())

            val left = (-offset.x) / scale
            val top = (-offset.y) / scale
            val right = (size.width - offset.x) / scale
            val bottom = (size.height - offset.y) / scale

            val x0 = floor(left / cell).toInt().coerceIn(0, w - 1)
            val y0 = floor(top / cell).toInt().coerceIn(0, h - 1)
            val x1 = ceil(right / cell).toInt().coerceIn(0, w - 1)
            val y1 = ceil(bottom / cell).toInt().coerceIn(0, h - 1)

            val effectiveCellPx = cell * scale
            val drawNumbers = effectiveCellPx >= 24f
            val drawGrid = effectiveCellPx >= 12f
            val gridStroke = androidx.compose.ui.graphics.drawscope.Stroke(
                width = (1f / scale).coerceAtLeast(0.5f)
            )

            for (yy in y0..y1) {
                val topY = yy * cell

                for (xx in x0..x1) {
                    val i = yy * w + xx
                    val idx = scheme.indices[i]
                    val color = scheme.palette[idx]

                    val leftX = xx * cell
                    val rectSize = Size(cell, cell)

                    drawRect(
                        color = color.copy(alpha = 0.18f),
                        topLeft = Offset(leftX, topY),
                        size = rectSize
                    )

                    if (drawGrid) {
                        drawRect(
                            color = Color.Black.copy(alpha = 0.25f),
                            topLeft = Offset(leftX, topY),
                            size = rectSize,
                            style = gridStroke
                        )
                    }

                    drawRect(
                        color = Color.Black.copy(alpha = 0.25f),
                        topLeft = Offset(leftX, topY),
                        size = rectSize,
                        style = gridStroke
                    )

                    if (drawNumbers) {
                        val number = (idx + 1).toString()
                        textPaint.color = Color.Black.copy(alpha = 0.75f).toArgb()
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