package ru.handhophop.feature.bookmark.presentation

internal data class BookmarkPhotoItem(
    val id: String,
    val photoUrl: String,
)

internal object BookmarkMemoryCache {
    private val cachedPhotos = emptyList<BookmarkPhotoItem>()

    fun getCachedPhotos(): List<BookmarkPhotoItem> = cachedPhotos
}

internal class BookmarkRepository(
    private val cache: BookmarkMemoryCache = BookmarkMemoryCache,
) {
    fun getCachedPhotos(): List<BookmarkPhotoItem> = cache.getCachedPhotos()
}
