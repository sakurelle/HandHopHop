package ru.handhophop.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ru.handhophop.data.local.UserPrefsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SelectedSchemeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserPrefsRepository(app.applicationContext)

    private val _selectedUrl = MutableStateFlow<String?>(null)
    val selectedUrl: StateFlow<String?> = _selectedUrl

    init {
        viewModelScope.launch {
            repo.selectedSchemeUrlFlow.collect { url ->
                _selectedUrl.value = url
            }
        }
    }

    fun select(url: String) {
        _selectedUrl.value = url
        viewModelScope.launch {
            repo.setSelectedSchemeUrl(url)
        }
    }
}