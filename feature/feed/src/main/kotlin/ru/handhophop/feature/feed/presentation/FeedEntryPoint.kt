package ru.handhophop.feature.feed.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.handhophop.core.network.UnsplashNetwork
import ru.handhophop.feature.feed.data.FeedRepository

@Composable
fun FeedEntryPoint() {
    val repository = FeedRepository(UnsplashNetwork.getApiService())
    val viewModel: FeedViewModel = viewModel(
        factory = FeedViewModel.Factory(repository)
    )
    FeedScreen(viewModel = viewModel)
}

@Preview(showBackground = true)
@Composable
fun FeedEntryPointPreview() {
    FeedEntryPoint()
}
