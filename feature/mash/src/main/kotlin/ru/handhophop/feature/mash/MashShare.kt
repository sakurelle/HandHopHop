package ru.handhophop.feature.mash

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

private const val SHARE_CARD_WIDTH = 1080
private const val SHARE_CARD_HEIGHT = 1350
private const val SHARE_CARD_FILE_NAME = "handhophop_result.png"

internal fun shareCompletedMashWork(
    context: Context,
    projectName: String?,
    previewImageBytes: ByteArray?,
) {
    val shareText = buildShareText(context, projectName)
    val shareIntent = runCatching {
        val cardUri = createShareCardUri(
            context = context,
            projectName = projectName,
            previewImageBytes = previewImageBytes,
        )

        Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_STREAM, cardUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }.getOrElse {
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
    }

    val chooser = Intent.createChooser(
        shareIntent,
        context.getString(R.string.mash_share_chooser_title),
    )
    context.startActivity(chooser)
}

private fun buildShareText(
    context: Context,
    projectName: String?,
): String {
    val normalizedProjectName = projectName?.takeIf { it.isNotBlank() }
    return if (normalizedProjectName == null) {
        context.getString(R.string.mash_share_text)
    } else {
        context.getString(R.string.mash_share_text_with_project, normalizedProjectName)
    }
}

private fun createShareCardUri(
    context: Context,
    projectName: String?,
    previewImageBytes: ByteArray?,
) = FileProvider.getUriForFile(
    context,
    "${context.packageName}.fileprovider",
    createShareCardFile(
        context = context,
        projectName = projectName,
        previewImageBytes = previewImageBytes,
    ),
)

private fun createShareCardFile(
    context: Context,
    projectName: String?,
    previewImageBytes: ByteArray?,
): File {
    val bitmap = Bitmap.createBitmap(
        SHARE_CARD_WIDTH,
        SHARE_CARD_HEIGHT,
        Bitmap.Config.ARGB_8888,
    )
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val projectTitle = projectName?.takeIf { it.isNotBlank() }
    val preview = previewImageBytes?.let {
        BitmapFactory.decodeByteArray(it, 0, it.size)
    }

    drawShareCard(
        context = context,
        canvas = canvas,
        paint = paint,
        projectName = projectTitle,
        preview = preview,
    )

    val shareDir = File(context.cacheDir, "share").apply {
        mkdirs()
    }
    val shareFile = File(shareDir, SHARE_CARD_FILE_NAME)
    FileOutputStream(shareFile).use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }
    preview?.recycle()
    bitmap.recycle()
    return shareFile
}

private fun drawShareCard(
    context: Context,
    canvas: Canvas,
    paint: Paint,
    projectName: String?,
    preview: Bitmap?,
) {
    val background = Color.rgb(255, 248, 239)
    val surface = Color.rgb(255, 255, 255)
    val primary = Color.rgb(137, 83, 53)
    val textPrimary = Color.rgb(55, 42, 34)
    val textSecondary = Color.rgb(112, 93, 81)
    val accent = Color.rgb(244, 196, 123)

    canvas.drawColor(background)

    paint.style = Paint.Style.FILL
    paint.color = accent
    canvas.drawCircle(940f, 132f, 180f, paint)
    paint.color = primary
    canvas.drawCircle(110f, 1220f, 220f, paint)

    val card = RectF(80f, 86f, 1000f, 1264f)
    paint.color = surface
    canvas.drawRoundRect(card, 48f, 48f, paint)

    paint.color = primary
    paint.textSize = 56f
    paint.isFakeBoldText = true
    canvas.drawText(context.getString(R.string.mash_share_card_brand), 140f, 180f, paint)
    paint.isFakeBoldText = false

    val previewRect = RectF(140f, 230f, 940f, 740f)
    paint.color = Color.rgb(248, 234, 215)
    canvas.drawRoundRect(previewRect, 40f, 40f, paint)
    if (preview != null) {
        drawPreview(canvas, paint, preview, previewRect)
    } else {
        paint.color = primary
        paint.textSize = 96f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("HHH", previewRect.centerX(), previewRect.centerY() + 32f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    var textY = 830f
    paint.color = textPrimary
    paint.textSize = 60f
    paint.isFakeBoldText = true
    textY = drawWrappedText(
        canvas = canvas,
        paint = paint,
        text = context.getString(R.string.mash_share_card_headline),
        x = 140f,
        y = textY,
        maxWidth = 800f,
        lineHeight = 72f,
    )
    paint.isFakeBoldText = false

    paint.color = textSecondary
    paint.textSize = 42f
    textY = drawWrappedText(
        canvas = canvas,
        paint = paint,
        text = context.getString(R.string.mash_share_card_subtitle),
        x = 140f,
        y = textY + 26f,
        maxWidth = 800f,
        lineHeight = 54f,
    )

    if (projectName != null) {
        paint.color = primary
        paint.textSize = 36f
        drawWrappedText(
            canvas = canvas,
            paint = paint,
            text = context.getString(R.string.mash_share_card_project, projectName),
            x = 140f,
            y = textY + 40f,
            maxWidth = 800f,
            lineHeight = 48f,
        )
    }

    paint.color = textSecondary
    paint.textSize = 34f
    canvas.drawText(context.getString(R.string.mash_share_card_body), 140f, 1168f, paint)
}

private fun drawPreview(
    canvas: Canvas,
    paint: Paint,
    preview: Bitmap,
    target: RectF,
) {
    val scale = maxOf(
        target.width() / preview.width.toFloat(),
        target.height() / preview.height.toFloat(),
    )
    val width = preview.width * scale
    val height = preview.height * scale
    val left = target.centerX() - width / 2f
    val top = target.centerY() - height / 2f
    val source = RectF(left, top, left + width, top + height)

    canvas.save()
    canvas.clipRect(target)
    canvas.drawBitmap(preview, null, source, paint)
    canvas.restore()
}

private fun drawWrappedText(
    canvas: Canvas,
    paint: Paint,
    text: String,
    x: Float,
    y: Float,
    maxWidth: Float,
    lineHeight: Float,
): Float {
    var currentY = y
    var line = ""

    text.split(" ").forEach { word ->
        val candidate = if (line.isBlank()) word else "$line $word"
        if (paint.measureText(candidate) <= maxWidth) {
            line = candidate
        } else {
            canvas.drawText(line, x, currentY, paint)
            currentY += lineHeight
            line = word
        }
    }

    if (line.isNotBlank()) {
        canvas.drawText(line.take(min(line.length, 90)), x, currentY, paint)
        currentY += lineHeight
    }

    return currentY
}
