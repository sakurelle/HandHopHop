package ru.handhophop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ru.handhophop.core.design.Route
import ru.handhophop.core.design.ScreenState
import ru.handhophop.core.design.TopBarState
import ru.handhophop.design.R as DesignR
import ru.handhophop.feature.feed.presentation.FeedEntryPoint
import ru.handhophop.feature.mash.MashEntryPoint

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val screenState = remember {
                    ScreenState(
                        Route.FEED,
                        TopBarState(R.string.main_screen_title, null, DesignR.drawable.filter)
                    )
                }

                ScreenBase(
                    screenState = screenState,
                    feedScreen = { FeedEntryPoint() },
                    mashScreen = { MashEntryPoint() },
                    bookmarkScreen = { TODO() },
                    settingsScreen = { TODO() }
                )
            }
        }
    }
}
