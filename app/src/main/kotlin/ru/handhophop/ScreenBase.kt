package ru.handhophop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.handhophop.core.design.BottomBar
import ru.handhophop.core.design.Route
import ru.handhophop.core.design.ScreenState
import ru.handhophop.core.design.TopBar
import ru.handhophop.core.design.TopBarState
import ru.handhophop.ui.BackgroundPattern


@Composable
internal fun ScreenBase(
    screenState: ScreenState,
    mashScreen: @Composable () -> Unit,
    settingsScreen: @Composable () -> Unit,
    feedScreen: @Composable () -> Unit,
    bookmarkScreen: @Composable () -> Unit,
    topBarState: TopBarState
) {
    val state by rememberUpdatedState(screenState)
    var currentRoute by remember { mutableStateOf(state.currentScreen) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    )
    {
        BackgroundPattern()

        //основной блок
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentRoute) {
                    Route.BOOKMARK -> {
                        bookmarkScreen()
                    }

                    Route.MASH -> {
                        mashScreen()
                    }

                    Route.FEED -> {
                        feedScreen()
                    }

                    Route.SETTINGS -> {
                        settingsScreen()
                    }
                }
            }
            BottomBar(
                currentRoute = currentRoute,
                onRouteSelected = { newRoute -> currentRoute = newRoute }
            )
        }
    }
}
