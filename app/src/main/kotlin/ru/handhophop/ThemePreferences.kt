package ru.handhophop

import android.content.Context
import ru.handhophop.core.design.ThemeMode
import androidx.core.content.edit

private const val THEME_PREFS_NAME = "theme_preferences"
private const val THEME_MODE_KEY = "theme_mode"

class ThemePreferences(context: Context) {
    private val preferences = context.getSharedPreferences(THEME_PREFS_NAME, Context.MODE_PRIVATE)

    fun getThemeMode(): ThemeMode {
        val rawValue = preferences.getString(THEME_MODE_KEY, ThemeMode.SYSTEM.name)
        return rawValue?.let {
            ThemeMode.entries.firstOrNull { mode -> mode.name == rawValue }
        } ?: ThemeMode.SYSTEM
    }

    fun setThemeMode(themeMode: ThemeMode) {
        preferences.edit {
            putString(THEME_MODE_KEY, themeMode.name)
        }
    }
}
