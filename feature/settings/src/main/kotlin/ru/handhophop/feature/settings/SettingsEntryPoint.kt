package ru.handhophop.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.handhophop.core.design.ThemeMode
import ru.handhophop.core.system.database.HandHopHopDatabaseProvider
import ru.handhophop.core.system.database.work.WorkLocalRepository


@Composable
fun SettingsEntryPoint(
    currentThemeMode: ThemeMode,
    isDarkTheme: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    val context = LocalContext.current
    val appContext = context.applicationContext // Используем appContext для ViewModel

    val repository = remember {
        WorkLocalRepository(
            workDao = HandHopHopDatabaseProvider.get(appContext).workDao(),
            appContext = appContext,
        )
    }

    // 2. Объявляем ViewModel с фабрикой
    val viewModel: SettingViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingViewModel(repository, appContext) as T
            }
        }
    )
    SettingScreen(
        currentThemeMode = currentThemeMode,
        isDarkTheme = isDarkTheme,
        onThemeModeChange = onThemeModeChange,
        viewModel = viewModel
    )
}
