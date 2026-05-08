package ru.handhophop.feature.mash

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresPermission
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ru.handhophop.feature.mash.MashCreate.MashThread
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor

private const val PDF_PAGE_WIDTH = 595
private const val PDF_PAGE_HEIGHT = 842
private const val PDF_MARGIN = 18f
private const val PDF_HEADER_HEIGHT = 34f
private const val PDF_FOOTER_HEIGHT = 18f
private const val PDF_DESIRED_CELL_SIZE = 9f
private const val PDF_GRID_FILL_ALPHA = 34
private const val PDF_MAJOR_GRID_STEP = 10
private const val PDF_PALETTE_COLUMNS = 8
private const val PDF_PALETTE_SWATCH_SIZE = 12f
private const val PDF_PALETTE_ROW_HEIGHT = 18f
private const val PDF_PALETTE_TITLE_HEIGHT = 14f
private const val PDF_NOTIFICATION_CHANNEL_ID = "mash_pdf_downloads"
private const val PDF_NOTIFICATION_ID = 1204

internal data class SavedPdfFile(
    val fileName: String,
    val uri: Uri,
)

internal fun exportSchemePdf(
    context: Context,
    projectTitle: String,
    scheme: SchemeData,
): Result<SavedPdfFile> = runCatching {
    val normalizedTitle = projectTitle.ifBlank {
        context.getString(R.string.mash_pdf_file_title_fallback)
    }
    val fileName = buildPdfFileName(normalizedTitle)
    val document = PdfDocument()
    var insertedUri: Uri? = null

    try {
        renderSchemePages(
            document = document,
            title = normalizedTitle,
            scheme = scheme,
        )

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${Environment.DIRECTORY_DOWNLOADS}/HandHopHop"
            )
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        insertedUri = context.contentResolver
            .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Unable to create MediaStore record")

        context.contentResolver.openOutputStream(insertedUri)?.use(document::writeTo)
            ?: error("Unable to open output stream")

        val publishValues = ContentValues().apply {
            put(MediaStore.MediaColumns.IS_PENDING, 0)
        }
        context.contentResolver.update(insertedUri, publishValues, null, null)

        SavedPdfFile(
            fileName = fileName,
            uri = insertedUri,
        )
    } catch (exception: Exception) {
        insertedUri?.let { uri ->
            context.contentResolver.delete(uri, null, null)
        }
        throw exception
    } finally {
        document.close()
    }
}

internal fun openSavedPdf(
    context: Context,
    savedPdfFile: SavedPdfFile,
) {
    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(savedPdfFile.uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooserIntent = Intent.createChooser(
        viewIntent,
        context.getString(R.string.mash_pdf_open_action),
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    if (viewIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(chooserIntent)
    }
}

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
internal fun showSavedPdfNotification(
    context: Context,
    savedPdfFile: SavedPdfFile,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val manager = NotificationManagerCompat.from(context)
    ensurePdfNotificationChannel(context)

    val openIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(savedPdfFile.uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        PDF_NOTIFICATION_ID,
        openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val notification = NotificationCompat.Builder(context, PDF_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_download_done)
        .setContentTitle(context.getString(R.string.mash_pdf_saved_title))
        .setContentText(savedPdfFile.fileName)
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(context.getString(R.string.mash_pdf_saved, savedPdfFile.fileName))
        )
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .addAction(
            0,
            context.getString(R.string.mash_pdf_open_action),
            pendingIntent,
        )
        .build()

    manager.notify(PDF_NOTIFICATION_ID, notification)
}

private fun ensurePdfNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val notificationManager = context.getSystemService(NotificationManager::class.java)
    val channel = NotificationChannel(
        PDF_NOTIFICATION_CHANNEL_ID,
        context.getString(R.string.mash_pdf_channel_name),
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = context.getString(R.string.mash_pdf_channel_description)
    }
    notificationManager.createNotificationChannel(channel)
}

private fun renderSchemePages(
    document: PdfDocument,
    title: String,
    scheme: SchemeData,
) {
    val paletteRows = ceil(scheme.palette.size / PDF_PALETTE_COLUMNS.toFloat()).toInt().coerceAtLeast(1)
    val paletteHeight = PDF_PALETTE_TITLE_HEIGHT + (paletteRows * PDF_PALETTE_ROW_HEIGHT)
    val usableWidth = PDF_PAGE_WIDTH - (PDF_MARGIN * 2)
    val usableHeight = PDF_PAGE_HEIGHT -
            (PDF_MARGIN * 2) -
            PDF_HEADER_HEIGHT -
            PDF_FOOTER_HEIGHT -
            paletteHeight

    val columnsPerPage = floor(usableWidth / PDF_DESIRED_CELL_SIZE).toInt()
        .coerceAtLeast(1)
        .coerceAtMost(scheme.gridW)
    val rowsPerPage = floor(usableHeight / PDF_DESIRED_CELL_SIZE).toInt()
        .coerceAtLeast(1)
        .coerceAtMost(scheme.gridH)
    val cellSize = minOf(
        usableWidth / columnsPerPage,
        usableHeight / rowsPerPage,
    )

    val titlePaint = createTextPaint(size = 16f, isBold = true)
    val infoPaint = createTextPaint(size = 9f)
    val numberPaint = createTextPaint(
        size = (cellSize * 0.48f).coerceAtLeast(5.5f),
        isBold = false,
        align = Paint.Align.CENTER,
    ).apply {
        color = AndroidColor.BLACK
    }
    val gridPaint = Paint().apply {
        color = AndroidColor.parseColor("#B8A89A")
        style = Paint.Style.STROKE
        strokeWidth = 0.6f
        isAntiAlias = true
    }
    val majorGridPaint = Paint().apply {
        color = AndroidColor.parseColor("#4C3E36")
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
        isAntiAlias = true
    }
    val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    var pageNumber = 1
    for (rowStart in 0 until scheme.gridH step rowsPerPage) {
        val rowEndExclusive = minOf(rowStart + rowsPerPage, scheme.gridH)
        val pageRows = rowEndExclusive - rowStart

        for (columnStart in 0 until scheme.gridW step columnsPerPage) {
            val columnEndExclusive = minOf(columnStart + columnsPerPage, scheme.gridW)
            val pageColumns = columnEndExclusive - columnStart

            val page = document.startPage(
                PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, pageNumber).create()
            )
            val canvas = page.canvas
            canvas.drawColor(AndroidColor.WHITE)

            val gridWidth = pageColumns * cellSize
            val gridHeight = pageRows * cellSize
            val gridLeft = PDF_MARGIN + ((usableWidth - gridWidth) / 2f)
            val gridTop = PDF_MARGIN + PDF_HEADER_HEIGHT

            canvas.drawText(title, PDF_MARGIN, PDF_MARGIN + 14f, titlePaint)
            canvas.drawText(
                "Колонки ${columnStart + 1}-$columnEndExclusive • Ряды ${rowStart + 1}-$rowEndExclusive",
                PDF_MARGIN,
                PDF_MARGIN + 28f,
                infoPaint,
            )

            for (row in rowStart until rowEndExclusive) {
                for (column in columnStart until columnEndExclusive) {
                    val schemeIndex = row * scheme.gridW + column
                    val paletteIndex = scheme.indices[schemeIndex]
                    val thread = scheme.palette[paletteIndex]
                    val left = gridLeft + ((column - columnStart) * cellSize)
                    val top = gridTop + ((row - rowStart) * cellSize)
                    val right = left + cellSize
                    val bottom = top + cellSize

                    fillPaint.color = withAlpha(thread.color.toArgb(), PDF_GRID_FILL_ALPHA)
                    canvas.drawRect(left, top, right, bottom, fillPaint)
                    canvas.drawRect(left, top, right, bottom, gridPaint)

                    val baseline = top + (cellSize / 2f) -
                            ((numberPaint.ascent() + numberPaint.descent()) / 2f)
                    canvas.drawText(
                        (paletteIndex + 1).toString(),
                        left + (cellSize / 2f),
                        baseline,
                        numberPaint,
                    )
                }
            }

            drawMajorGrid(
                canvas = canvas,
                left = gridLeft,
                top = gridTop,
                cellSize = cellSize,
                columnStart = columnStart,
                columnEndExclusive = columnEndExclusive,
                rowStart = rowStart,
                rowEndExclusive = rowEndExclusive,
                majorGridPaint = majorGridPaint,
            )

            renderPaletteFooter(
                canvas = canvas,
                palette = scheme.palette,
                top = PDF_PAGE_HEIGHT - PDF_MARGIN - PDF_FOOTER_HEIGHT - paletteHeight,
                left = PDF_MARGIN,
                width = usableWidth,
            )

            canvas.drawText(
                "Страница $pageNumber",
                PDF_MARGIN,
                PDF_PAGE_HEIGHT - PDF_MARGIN,
                infoPaint,
            )

            document.finishPage(page)
            pageNumber++
        }
    }
}

private fun drawMajorGrid(
    canvas: Canvas,
    left: Float,
    top: Float,
    cellSize: Float,
    columnStart: Int,
    columnEndExclusive: Int,
    rowStart: Int,
    rowEndExclusive: Int,
    majorGridPaint: Paint,
) {
    val verticalStart = (columnStart / PDF_MAJOR_GRID_STEP) * PDF_MAJOR_GRID_STEP
    var majorColumn = verticalStart
    while (majorColumn <= columnEndExclusive) {
        val lineX = left + ((majorColumn - columnStart) * cellSize)
        canvas.drawLine(
            lineX,
            top,
            lineX,
            top + ((rowEndExclusive - rowStart) * cellSize),
            majorGridPaint,
        )
        majorColumn += PDF_MAJOR_GRID_STEP
    }

    val horizontalStart = (rowStart / PDF_MAJOR_GRID_STEP) * PDF_MAJOR_GRID_STEP
    var majorRow = horizontalStart
    while (majorRow <= rowEndExclusive) {
        val lineY = top + ((majorRow - rowStart) * cellSize)
        canvas.drawLine(
            left,
            lineY,
            left + ((columnEndExclusive - columnStart) * cellSize),
            lineY,
            majorGridPaint,
        )
        majorRow += PDF_MAJOR_GRID_STEP
    }
}

private fun renderPaletteFooter(
    canvas: Canvas,
    palette: List<MashThread>,
    top: Float,
    left: Float,
    width: Float,
) {
    val titlePaint = createTextPaint(size = 9f, isBold = true)
    val itemPaint = createTextPaint(size = 7f)
    val swatchFillPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    val swatchBorderPaint = Paint().apply {
        color = AndroidColor.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 0.8f
        isAntiAlias = true
    }
    val itemWidth = width / PDF_PALETTE_COLUMNS

    canvas.drawText("Палитра", left, top + 10f, titlePaint)

    palette.forEachIndexed { index, thread ->
        val column = index % PDF_PALETTE_COLUMNS
        val row = index / PDF_PALETTE_COLUMNS
        val itemLeft = left + (column * itemWidth)
        val itemTop = top + PDF_PALETTE_TITLE_HEIGHT + (row * PDF_PALETTE_ROW_HEIGHT)

        swatchFillPaint.color = thread.color.toArgb()
        canvas.drawRect(
            itemLeft,
            itemTop,
            itemLeft + PDF_PALETTE_SWATCH_SIZE,
            itemTop + PDF_PALETTE_SWATCH_SIZE,
            swatchFillPaint,
        )
        canvas.drawRect(
            itemLeft,
            itemTop,
            itemLeft + PDF_PALETTE_SWATCH_SIZE,
            itemTop + PDF_PALETTE_SWATCH_SIZE,
            swatchBorderPaint,
        )

        val baseline = itemTop + (PDF_PALETTE_SWATCH_SIZE / 2f) -
                ((itemPaint.ascent() + itemPaint.descent()) / 2f)
        canvas.drawText(
            "${index + 1} ${thread.article}",
            itemLeft + PDF_PALETTE_SWATCH_SIZE + 4f,
            baseline,
            itemPaint,
        )
    }
}

private fun createTextPaint(
    size: Float,
    isBold: Boolean = false,
    align: Paint.Align = Paint.Align.LEFT,
): Paint = Paint().apply {
    color = AndroidColor.BLACK
    textSize = size
    textAlign = align
    isAntiAlias = true
    style = Paint.Style.FILL
    typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
}

private fun buildPdfFileName(title: String): String {
    val safeTitle = title
        .trim()
        .ifBlank { "scheme" }
        .replace(Regex("[\\\\/:*?\"<>|]"), "_")
        .replace(Regex("\\s+"), "_")

    return "Схема_${safeTitle}.pdf"
}

private fun withAlpha(color: Int, alpha: Int): Int {
    val red = AndroidColor.red(color)
    val green = AndroidColor.green(color)
    val blue = AndroidColor.blue(color)
    return AndroidColor.argb(alpha, red, green, blue)
}
