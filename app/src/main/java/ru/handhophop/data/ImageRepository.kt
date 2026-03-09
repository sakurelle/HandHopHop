package ru.handhophop.data

import ru.handhophop.data.remote.NekoApi
import ru.handhophop.data.remote.NekoImageDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

data class ImageItem(
    val id: String,
    val imageUrl: String,
    val aspectRatio: Float,
    val author: String = ""
)

class ImageRepository(
    private val api: NekoApi
) {
    private val cache = mutableMapOf<Int, List<ImageItem>>()
    private var nextId: Int = 1

    private suspend fun loadNextSafeImage(): ImageItem {
        var attempts = 0

        while (attempts < 50) {
            val currentId = nextId
            nextId += 1
            attempts += 1

            val dto: NekoImageDto = try {
                api.getImageById(currentId)
            } catch (_: HttpException) {
                continue
            } catch (_: Exception) {
                continue
            }

            if (dto.rating != "safe") continue

            return ImageItem(
                id = dto.id.toString(),
                imageUrl = dto.url,
                aspectRatio = 0f,
                author = dto.artist_name ?: ""
            )
        }

        throw IllegalStateException("Не удалось получить safe изображение")
    }

    suspend fun loadPage(page: Int, pageSize: Int): List<ImageItem> =
        withContext(Dispatchers.IO) {
            if (page == 1) {
                nextId = 1
                cache.clear()
            }

            cache[page]?.let { return@withContext it }

            val result = mutableListOf<ImageItem>()
            repeat(pageSize) { result += loadNextSafeImage() }

            cache[page] = result
            result
        }
}