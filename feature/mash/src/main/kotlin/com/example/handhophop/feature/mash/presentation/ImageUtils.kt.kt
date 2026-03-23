package com.example.handhophop.feature.mash.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import androidx.core.graphics.createBitmap

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