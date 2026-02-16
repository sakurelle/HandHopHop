package com.example.handhophop.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handhophop.data.ImageItem
import com.example.handhophop.data.ImageRepository
import com.example.handhophop.data.remote.NekoNetwork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.min

data class ImageListState(
    val items: List<ImageItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val reachedEnd: Boolean = false,
    val isSquareFilterEnabled: Boolean = false
) {
    // Вычисляемый список на основе фильтра
    val filteredItems: List<ImageItem>
        get() = if (isSquareFilterEnabled) {
            // Квадратными считаем те, у кого соотношение сторон от 0.9 до 1.1
            items.filter { it.aspectRatio < 0.5f }
        } else {
            // Прямоугольными считаем те, которые явно не квадратные
            items
        }
}

class OnlineSchemesViewModel(
    private val repo: ImageRepository = ImageRepository(NekoNetwork.api)
) : ViewModel() {

    companion object {
        private const val MAX_ITEMS = 100 // Увеличил лимит, чтобы было из чего фильтровать
        private const val PAGE_SIZE = 20
    }

    private val _state = MutableStateFlow(ImageListState())
    val state: StateFlow<ImageListState> = _state.asStateFlow()

    private var page = 1

    init {
        loadMore()
    }

    fun toggleFilter(isEnabled: Boolean) {
        _state.update { it.copy(isSquareFilterEnabled = isEnabled) }
    }

    fun loadMore() {
        val cur = _state.value
        if (cur.isLoading || cur.reachedEnd) return

        val already = cur.items.size
        if (already >= MAX_ITEMS) {
            _state.update { it.copy(reachedEnd = true) }
            return
        }

        val requestSize = min(PAGE_SIZE, MAX_ITEMS - already)

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val newItems = repo.loadPage(page, requestSize)
                if (newItems.isEmpty()) {
                    _state.update { it.copy(isLoading = false, reachedEnd = true) }
                } else {
                    page += 1
                    _state.update { currentState ->
                        val merged = currentState.items + newItems
                        currentState.copy(
                            items = merged,
                            isLoading = false,
                            reachedEnd = merged.size >= MAX_ITEMS
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Ошибка загрузки") }
            }
        }
    }

    fun refresh() {
        page = 1
        _state.value = ImageListState(isSquareFilterEnabled = _state.value.isSquareFilterEnabled)
        loadMore()
    }
}
