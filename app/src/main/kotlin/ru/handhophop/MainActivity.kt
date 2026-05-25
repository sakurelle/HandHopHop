package ru.handhophop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.core.design.HandHopHopDesignTheme
import ru.handhophop.core.design.ThemeMode
import ru.handhophop.core.network.voucher.VoucherNetworkService
import ru.handhophop.core.session.PremiumProvider
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
        val themePreferences = ThemePreferences(applicationContext)
        setContent {
            PremiumProvider.init(applicationContext)
            val systemIsDarkTheme = isSystemInDarkTheme()
            var themeMode by remember {
                mutableStateOf(themePreferences.getThemeMode())
            }
            val isDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemIsDarkTheme
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            LaunchedEffect(Unit) {
                PremiumProvider.ensureUserHashExists()
                PremiumProvider.isPremium()
            }

            HandHopHopDesignTheme(isDarkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = HandHopHopDesignSystem.colors.background
                ) {
                    ScreenBase(
                        feedScreen = { onPhotoSelected -> FeedEntryPoint(onPhotoSelected = onPhotoSelected) },
                        mashScreen = { initialWorkId, initialImageUrl,backgroundContent, onBack, onBottomBarVisibilityChanged, onOpenFeed ->
                            MashEntryPoint(
                                initialWorkId = initialWorkId,
                                initialImageUrl = initialImageUrl,
                                backgroundContent = backgroundContent,
                                onBack = onBack,
                                onBottomBarVisibilityChanged = onBottomBarVisibilityChanged,
                                onOpenFeed = onOpenFeed
                            )
                        },
                        bookmarkScreen = { onPhotoSelected -> BookmarkEntryPoint(onPhotoSelected = onPhotoSelected) },
                        settingsScreen = {
                            SettingsEntryPoint(
                                currentThemeMode = themeMode,
                                isDarkTheme = isDarkTheme,
                                onThemeModeChange = { newThemeMode ->
                                    themeMode = newThemeMode
                                    themePreferences.setThemeMode(newThemeMode)
                                }
                            )
                        }
                    )
                }
            }

        }
    }
}
