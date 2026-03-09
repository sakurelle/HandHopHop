package ru.handhophop.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ru.handhophop.data.local.ProfileState
import ru.handhophop.data.local.UserPrefsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserPrefsRepository(app.applicationContext)

    private val _state = MutableStateFlow(
        ProfileState(
            name = "abober4000",
            login = "abober4000",
            email = "",
            phone = "",
            language = "Русский",
            avatarUri = null
        )
    )
    val state: StateFlow<ProfileState> = _state

    init {
        viewModelScope.launch {
            repo.profileFlow.collect { loaded ->
                _state.value = loaded
            }
        }
    }

    fun update(transform: (ProfileState) -> ProfileState) {
        val newState = transform(_state.value)
        _state.value = newState
        viewModelScope.launch { repo.saveProfile(newState) }
    }

    fun setAvatarFromPicker(uri: Uri) {
        viewModelScope.launch {
            val saved = saveAvatarToInternalStorage(uri) ?: return@launch
            update { it.copy(avatarUri = saved) } // update уже сохранит в DataStore
        }
    }

    private suspend fun saveAvatarToInternalStorage(srcUri: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                val ctx = getApplication<Application>().applicationContext

                // если раньше был сохранён file:// — можно удалить старый файл
                _state.value.avatarUri?.let { old ->
                    runCatching {
                        if (old.startsWith("file://")) {
                            val oldFile = File(Uri.parse(old).path ?: return@runCatching)
                            if (oldFile.exists()) oldFile.delete()
                        }
                    }
                }

                val dir = File(ctx.filesDir, "avatars").apply { mkdirs() }
                val outFile = File(dir, "avatar.jpg")

                ctx.contentResolver.openInputStream(srcUri)?.use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: return@withContext null

                // сохраняем file://... чтобы Coil мог читать всегда
                Uri.fromFile(outFile).toString()
            } catch (_: Exception) {
                null
            }
        }
}