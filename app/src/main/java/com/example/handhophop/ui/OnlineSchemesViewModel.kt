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
    val filteredItems: List<ImageItem>
        get() = if (isSquareFilterEnabled) {
            // квадрат ~ 1.0 (0.9..1.1)
            items.filter { it.aspectRatio > 0f && it.aspectRatio in 0.9f..1.1f }
        } else {
            items
        }
}

class OnlineSchemesViewModel(
    private val repo: ImageRepository = ImageRepository(NekoNetwork.api)
) : ViewModel() {

    companion object {
        private const val MAX_ITEMS = 500
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
                    _state.update { st ->
                        val merged = st.items + newItems
                        st.copy(
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