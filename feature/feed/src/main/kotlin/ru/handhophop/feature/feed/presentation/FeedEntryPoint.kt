package ru.handhophop.feature.feed.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.handhophop.core.network.FreepikNetwork
import ru.handhophop.core.system.database.HandHopHopDatabaseProvider
import ru.handhophop.core.system.database.work.WorkLocalRepository
import ru.handhophop.feature.feed.data.FeedRepository

@Composable
fun FeedEntryPoint(
    onPhotoSelected: (String) -> Unit = {},
) {
    val context = LocalContext.current

    val repository = remember(context) {
        FeedRepository(
            apiService = FreepikNetwork.getApiService(),
            workLocalRepository = WorkLocalRepository(
                workDao = HandHopHopDatabaseProvider.get(context).workDao()
            )
        )
    }

    val viewModel: FeedViewModel = viewModel(
        factory = FeedViewModel.Factory(repository)
    )
    FeedScreen(
        viewModel = viewModel,
        onPhotoSelected = onPhotoSelected,
    )
}

@Preview(showBackground = true)
@Composable
fun FeedEntryPointPreview() {
    FeedEntryPoint()
}
