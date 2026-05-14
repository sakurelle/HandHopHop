package ru.handhophop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.core.design.HandHopHopDesignTheme
import ru.handhophop.feature.bookmark.presentation.BookmarkEntryPoint
import ru.handhophop.feature.feed.presentation.FeedEntryPoint
import ru.handhophop.feature.mash.MashEntryPoint
import ru.handhophop.feature.settings.SettingsEntryPoint

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = ContextCompat.getColor(this, ru.handhophop.design.R.color.main_color),
                darkScrim = ContextCompat.getColor(
                    this,
                    ru.handhophop.design.R.color.main_color_dark
                ),
            )
        )
        super.onCreate(savedInstanceState)
        setContent {
            HandHopHopDesignTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = HandHopHopDesignSystem.colors.surfaceSoft
                ) {
                    ScreenBase(
                        feedScreen = { onPhotoSelected -> FeedEntryPoint(onPhotoSelected = onPhotoSelected) },
                        mashScreen = { initialWorkId, initialImageUrl,backgroundContent, onBack, onBottomBarVisibilityChanged ->
                            MashEntryPoint(
                                initialWorkId = initialWorkId,
                                initialImageUrl = initialImageUrl,
                                backgroundContent = backgroundContent,
                                onBack = onBack,
                                onBottomBarVisibilityChanged = onBottomBarVisibilityChanged
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
