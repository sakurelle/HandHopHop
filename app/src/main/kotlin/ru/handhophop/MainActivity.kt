package ru.handhophop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import ru.handhophop.core.design.HandHopHopDesignTheme
import ru.handhophop.feature.bookmark.presentation.BookmarkEntryPoint
import ru.handhophop.feature.feed.presentation.FeedEntryPoint
import ru.handhophop.feature.mash.MashEntryPoint
import ru.handhophop.feature.settings.SettingsEntryPoint

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = ContextCompat.getColor(this, ru.handhophop.design.R.color.black),//не работает почему-то
                darkScrim = ContextCompat.getColor(this, ru.handhophop.design.R.color.main_color),//todo
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = ContextCompat.getColor(this, ru.handhophop.design.R.color.main_color),
                darkScrim = ContextCompat.getColor(this, ru.handhophop.design.R.color.main_color),//todo
            )
        )
        super.onCreate(savedInstanceState)
        setContent {
            HandHopHopDesignTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScreenBase(
                        feedScreen = { onPhotoSelected -> FeedEntryPoint(onPhotoSelected = onPhotoSelected) },
                        mashScreen = { initialWorkId, initialImageUrl, onBottomBarVisibilityChanged ->
                            MashEntryPoint(
                                initialWorkId = initialWorkId,
                                initialImageUrl = initialImageUrl,
                                onBottomBarVisibilityChanged = onBottomBarVisibilityChanged,
                            )
                        },
                        bookmarkScreen = { onPhotoSelected -> BookmarkEntryPoint(onPhotoSelected = onPhotoSelected) },
                        settingsScreen = { SettingsEntryPoint() }
                    )
                }
            }
        }
    }
}
