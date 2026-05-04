package ru.handhophop.feature.mash

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

internal suspend fun loadBitmapFromUrl(
    context: Context,
    url: String
): Bitmap? = withContext(Dispatchers.IO) {
    runCatching {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()

        val result = loader.execute(request)
        if (result is SuccessResult) {
            drawableToBitmap(result.drawable)
        } else {
            null
        }
    }.getOrNull()
}

internal suspend fun loadDefaultBitmap(
    context: Context,
    @DrawableRes drawableRes: Int
): Bitmap? = withContext(Dispatchers.IO) {
    runCatching {
        ContextCompat.getDrawable(context, drawableRes)?.let(::drawableToBitmap)
    }.getOrNull()
}

internal fun bitmapToByteArray(
    bitmap: Bitmap,
): ByteArray? = runCatching {
    ByteArrayOutputStream().use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.toByteArray()
    }
}.getOrNull()

internal fun byteArrayToBitmap(
    bytes: ByteArray,
): Bitmap? = runCatching {
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}.getOrNull()

private fun drawableToBitmap(drawable: Drawable): Bitmap? =
    runCatching {
        if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val w = max(1, drawable.intrinsicWidth)
            val h = max(1, drawable.intrinsicHeight)
            val bitmap = createBitmap(w, h)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }.getOrNull()
