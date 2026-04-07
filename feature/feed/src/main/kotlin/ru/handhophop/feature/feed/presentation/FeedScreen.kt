package ru.handhophop.feature.feed.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun FeedScreen(viewModel: FeedViewModel) {
    LaunchedEffect(viewModel) {
        viewModel.handleAction(FeedUiAction.LoadPhotos)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is FeedUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is FeedUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${state.msg}")
            }
        }
        is FeedUiState.Success -> {
            FeedGrid(
                state = state,
                onPhotoClicked = { id -> viewModel.handleAction(FeedUiAction.PhotoClicked(id))},
                onLoadMore = {viewModel.handleAction(FeedUiAction.LoadNextPage)}
            )
        }
    }
}