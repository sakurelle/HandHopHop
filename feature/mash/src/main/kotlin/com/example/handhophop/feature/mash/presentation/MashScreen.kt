package com.example.handhophop.feature.mash.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable

//TODO композабл или SchemeScreen

@Composable
internal fun MashScreen(
    viewModel: MashViewModel
) {
    val uiState: MashUiState by viewModel.uiState.collectAsState(initial = MashUiState())

    if (uiState.isLoading) {
        CircularProgressIndicator()
    }

    Box(
        modifier = Modifier.clickable {
            viewModel.handleAction(ClickDownloadsAction())
        }
    ) {
    }
}