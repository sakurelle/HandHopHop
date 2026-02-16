package com.example.handhophop.data

import com.example.handhophop.data.remote.NekoApi
import com.example.handhophop.data.remote.NekoImageDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import kotlin.random.Random

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

    /**
     * Запрашивает следующее изображение по id и пропускает:
     * - HTTP ошибки (404/500)
     * - любые рейтинги кроме "safe"
     * Возвращает первый найденный safe-результат.
     */
    private suspend fun loadNextSafeImage(): ImageItem {
        var attempts = 0

        while (attempts < 50) {
            val currentId = nextId
            nextId += 1
            attempts += 1

            val dto: NekoImageDto = try {
                api.getImageById(currentId)
            } catch (e: HttpException) {
                continue
            } catch (e: Exception) {
                continue
            }

            if (dto.rating != "safe") continue

            return ImageItem(
                id = dto.id.toString(),
                imageUrl = dto.url,
                aspectRatio = Random.nextFloat()
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
