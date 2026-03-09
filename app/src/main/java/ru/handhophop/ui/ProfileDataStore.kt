package ru.handhophop.ui

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("profile_prefs")

object ProfileKeys {
    val NAME = stringPreferencesKey("name")
    val LOGIN = stringPreferencesKey("login")
    val EMAIL = stringPreferencesKey("email")
    val PHONE = stringPreferencesKey("phone")
    val LANGUAGE = stringPreferencesKey("language")
    val AVATAR_URI = stringPreferencesKey("avatar_uri")
}

class ProfileRepository(private val context: Context) {
    val profileFlow: Flow<ProfileState> = context.dataStore.data.map { p ->
        ProfileState(
            name = p[ProfileKeys.NAME] ?: "abober4000",
            login = p[ProfileKeys.LOGIN] ?: "abober4000",
            email = p[ProfileKeys.EMAIL] ?: "egor@loh.ru",
            phone = p[ProfileKeys.PHONE] ?: "",
            language = p[ProfileKeys.LANGUAGE] ?: "Русский",
            avatarUri = p[ProfileKeys.AVATAR_URI]
        )
    }

    suspend fun save(state: ProfileState) {
        context.dataStore.edit { p ->
            p[ProfileKeys.NAME] = state.name
            p[ProfileKeys.LOGIN] = state.login
            p[ProfileKeys.EMAIL] = state.email
            p[ProfileKeys.PHONE] = state.phone
            p[ProfileKeys.LANGUAGE] = state.language
            state.avatarUri?.let { p[ProfileKeys.AVATAR_URI] = it } ?: p.remove(ProfileKeys.AVATAR_URI)
        }
    }
}

data class ProfileState(
    val name: String,
    val login: String,
    val email: String,
    val phone: String,
    val language: String,
    val avatarUri: String? = null
)
