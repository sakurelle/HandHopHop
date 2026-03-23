package com.example.handhophop.feature.mash.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
internal fun ScreenBase(
    screenState: ScreenState,
    MashScreen: @Composable () -> Unit,
    ProfileScreen: @Composable () -> Unit,
    FeedScreen: @Composable () -> Unit,
    BookmarkScreen: @Composable () -> Unit,
    topBarState: TopBarState
) {
    //val mainPadding = dimensionResource(R.dimen.main_padding)
    var currentRoute by remember { mutableStateOf(screenState.route) }

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
            TopBar(topBarState)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (screenState.route) {
                    Route.BOOKMARK -> {
                        BookmarkScreen()
                    }

                    Route.MASH -> {
                        MashScreen()
                    }

                    Route.FEED -> {
                        FeedScreen()
                    }

                    Route.PROFILE -> {
                        ProfileScreen()
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
