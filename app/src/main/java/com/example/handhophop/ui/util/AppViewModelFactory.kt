package com.example.handhophop.ui.util

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.handhophop.ui.ProfileViewModel
import com.example.handhophop.ui.SelectedSchemeViewModel

class AppViewModelFactory(private val app: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(app) as T

            modelClass.isAssignableFrom(SelectedSchemeViewModel::class.java) ->
                SelectedSchemeViewModel(app) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

@Composable
fun rememberAppViewModelFactory(): ViewModelProvider.Factory {
    val app = LocalContext.current.applicationContext as Application
    return remember(app) { AppViewModelFactory(app) }
}