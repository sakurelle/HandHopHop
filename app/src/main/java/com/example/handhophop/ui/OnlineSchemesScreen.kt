package com.example.handhophop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.error
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.handhophop.data.ImageItem
// Исправлен импорт (убрана опечатка uiimport)
import com.example.handhophop.ui.OnlineSchemesViewModel
//import com.example.handhophop.uiimport.OnlineSchemesViewModel

@Composable
fun OnlineSchemesScreen(
    selectedVm: SelectedSchemeViewModel,
    navController: NavHostController,
    viewModel: OnlineSchemesViewModel = viewModel(),
    onItemClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // --- БЛОК ФИЛЬТРАЦИИ ---
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.isSquareFilterEnabled,
                    onCheckedChange = { viewModel.toggleFilter(it) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isSquareFilterEnabled) "Только квадратные" else "Все форматы",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // --- СПИСОК ---
        // Используем weight(1f), чтобы список не вытеснял чекбокс и не пропадал
        OnlineSchemesMasonry(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            items = state.filteredItems,
            isLoading = state.isLoading,
            reachedEnd = state.reachedEnd,
            error = state.error,
            onNeedMore = { viewModel.loadMore() },
            onClickItem = { item ->
                onItemClick(item.imageUrl)
            }
        )
    }
}

@Composable
fun OnlineSchemesMasonry(
    modifier: Modifier = Modifier,
    items: List<ImageItem>,
    isLoading: Boolean,
    reachedEnd: Boolean,
    error: String?,
    onNeedMore: () -> Unit,
    onClickItem: (ImageItem) -> Unit
) {
    Box(modifier = modifier) {
        // Если список пуст после фильтрации
        if (items.isEmpty() && !isLoading) {
            Text(
                text = "Нет изображений для этого фильтра",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            // Добавлен KEY для корректного обновления элементов при фильтрации
            items(items, key = { it.imageUrl }) { item ->
                Card(
                    onClick = { onClickItem(item) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        // Важно для Masonry: сохраняем пропорции картинки
                        contentScale = ContentScale.FillWidth
                    )
                }
            }

            // Подгрузка данных
            if (!reachedEnd && items.isNotEmpty()) {
                item {
                    LaunchedEffect(items.size) {
                        onNeedMore()
                    }
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)
            )
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        }
    }
}
