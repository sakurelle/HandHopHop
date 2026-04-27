package ru.handhophop.feature.mash

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.handhophop.feature.mash.MashCreate.MashCreateConfig
import ru.handhophop.feature.mash.MashCreate.MashCreateScreen

@Composable
fun MashEntryPoint(
    initialImageUrl: String? = null,
) {
    val viewModel: MashViewModel = viewModel()
    var createdConfig by remember { mutableStateOf<MashCreateConfig?>(null) }

    BackHandler(enabled = createdConfig != null) {
        createdConfig = null
    }

    val config = createdConfig
    if (config == null) {
        MashCreateScreen(
            imageUrl = initialImageUrl,
            onCreateFinished = { newConfig ->
                createdConfig = newConfig
            },
            topBar = { _, _, _ -> }
        )
    } else {
        MashScreen(
            viewModel = viewModel,
            config = config,
        )
    }
}
