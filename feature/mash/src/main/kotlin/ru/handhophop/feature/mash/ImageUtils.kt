package ru.handhophop.feature.mash

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.graphics.createBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

private const val MASH_IMAGES_DIRECTORY = "mash_images"

internal data class SavedLocalImage(
    val path: String,
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SavedLocalImage

        if (path != other.path) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

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

internal fun readImageBytesFromFile(
    imagePath: String?,
): ByteArray? = runCatching {
    imagePath
        ?.takeIf { it.isNotBlank() }
        ?.let(::File)
        ?.takeIf(File::exists)
        ?.readBytes()
}.getOrNull()

internal suspend fun copyPickedImageToAppStorage(
    context: Context,
    uri: Uri,
): SavedLocalImage? = withContext(Dispatchers.IO) {
    runCatching {
        val bytes = context.contentResolver.openInputStream(uri)
            ?.use { stream -> stream.readBytes() }
            ?: throw IOException("Unable to open picked image stream")

        if (bytes.isEmpty()) {
            throw IOException("Picked image is empty")
        }

        val bitmap = byteArrayToBitmap(bytes)
            ?: throw IOException("Picked image cannot be decoded")

        val normalizedBytes = bitmapToByteArray(bitmap)
            ?.takeIf { it.isNotEmpty() }
            ?: throw IOException("Picked image cannot be encoded")

        val imageDirectory = File(context.filesDir, MASH_IMAGES_DIRECTORY)
        if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
            throw IOException("Unable to create mash image directory")
        }

        val imageFile = File(imageDirectory, "${UUID.randomUUID()}.jpg")
        imageFile.writeBytes(normalizedBytes)

        SavedLocalImage(
            path = imageFile.absolutePath,
            bytes = normalizedBytes,
        )
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
