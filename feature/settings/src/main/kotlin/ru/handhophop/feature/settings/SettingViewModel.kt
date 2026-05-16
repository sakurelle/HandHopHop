package ru.handhophop.feature.settings

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.handhophop.core.system.database.work.WorkLocalRepository

class SettingViewModel(
    private val repository: WorkLocalRepository,
    private val context: Context
) : ViewModel() {

    private val _storageText = mutableStateOf("0 mb")
    val storageText: State<String> = _storageText

    private val _storageProgress = mutableFloatStateOf(0f)
    val storageProgress: State<Float> = _storageProgress

    private val MAX_SIZE_MB = 512f

    init {
        updateStorageStats()
    }

    fun updateStorageStats() {
        viewModelScope.launch {
            val bytes = repository.getDatabaseSize(context)
            val count = repository.getWorkCount()
            val megabytes = bytes / (1024 * 1024).toDouble()

            _storageText.value = when {
                count == 0 -> "0 mb"
                megabytes < 0.1 -> "%.2f mb".format(megabytes)
                else -> "%.1f mb".format(megabytes)
            }

            val progress = (megabytes.toFloat() / MAX_SIZE_MB).coerceIn(0f, 1f)
            _storageProgress.floatValue = if (count == 0) 0f else progress
        }
    }

    fun clearDatabase() {
        viewModelScope.launch {
            repository.clearAllWorks()
            updateStorageStats()
        }
    }
}