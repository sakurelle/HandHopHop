package com.example.handhophop.feature.mash.presentation

//TODO композабл или SchemeScreen

@Composable
internal fun MashScreen(
    viewModel: MashViewModel
) {

    val uiState = viewModel.uiState.collectAsState()

    if (uiState.isLoading == true) {
        ProgressBar() {

        }
    }

    //кнопка загрузки
    Box(
        onClick = {
            viewModel.handleAction(ClickDownloadsAction())
        }
    ) {

    }
}