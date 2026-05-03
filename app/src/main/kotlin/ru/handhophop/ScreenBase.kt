package ru.handhophop

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ru.handhophop.core.design.BackgroundPattern
import ru.handhophop.core.design.BottomBar
import ru.handhophop.core.design.Route


@Composable
internal fun ScreenBase(
    feedScreen: @Composable (onPhotoSelected: (String) -> Unit) -> Unit,
    mashScreen: @Composable (String?, (Boolean) -> Unit) -> Unit,
    bookmarkScreen: @Composable () -> Unit,
    settingsScreen: @Composable () -> Unit,
) {
    @Suppress("UNCHECKED_CAST")
    val backStack = rememberNavBackStack(AppRoute.Feed) as NavBackStack<AppRoute>
    var isBottomBarVisible by remember { mutableStateOf(true) }
    val appEntryProvider = remember(mashScreen, settingsScreen, feedScreen, bookmarkScreen) {
        entryProvider {
            entry<AppRoute.Bookmark> { bookmarkScreen() }
            entry<AppRoute.Feed> {
                feedScreen { imageUrl ->
                    backStack.add(AppRoute.Mash(imageUrl = imageUrl))
                }
            }
            entry<AppRoute.Mash> { key ->
                mashScreen(key.imageUrl) { isVisible ->
                    isBottomBarVisible = isVisible
                }
            }
            entry<AppRoute.Settings> { settingsScreen() }
        }
    }
    BackHandler(enabled = backStack.size > 1) {
        backStack.removeAt(backStack.lastIndex)
    }
    val currentRoute = (backStack.lastOrNull() as? AppRoute)?.tab ?: AppRoute.Feed.tab

    LaunchedEffect(currentRoute) {
        if (currentRoute != Route.MASH) {
            isBottomBarVisible = true
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
    )
    {
        BackgroundPattern()

        //основной блок
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider = appEntryProvider
                )
            }
            if (isBottomBarVisible) {
                BottomBar(
                    currentRoute = currentRoute,
                    onRouteSelected = { newRoute ->
                        val destination = AppRoute.from(newRoute)
                        if (backStack.lastOrNull() != destination) {
                            backStack.clear()
                            backStack.add(destination)
                        }
                    }
                )
            }
        }
    }
}
