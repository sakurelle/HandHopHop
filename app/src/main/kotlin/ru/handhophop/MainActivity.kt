package ru.handhophop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ru.handhophop.feature.bookmark.presentation.BookmarkEntryPoint
import ru.handhophop.feature.feed.presentation.FeedEntryPoint
import ru.handhophop.feature.mash.MashEntryPoint
import ru.handhophop.feature.settings.SettingsEntryPoint

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                ScreenBase(
                    feedScreen = { onPhotoSelected -> FeedEntryPoint(onPhotoSelected = onPhotoSelected) },
                    mashScreen = { initialImageUrl -> MashEntryPoint(initialImageUrl = initialImageUrl) },
                    bookmarkScreen = { BookmarkEntryPoint() },
                    settingsScreen = { SettingsEntryPoint() }
                )
            }
        }
    }
}
