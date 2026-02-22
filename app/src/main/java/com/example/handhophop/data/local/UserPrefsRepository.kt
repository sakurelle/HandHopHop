package com.example.handhophop.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "handhophop_prefs")

object PrefKeys {
    val NAME = stringPreferencesKey("name")
    val LOGIN = stringPreferencesKey("login")
    val EMAIL = stringPreferencesKey("email")
    val PHONE = stringPreferencesKey("phone")
    val LANGUAGE = stringPreferencesKey("language")
    val AVATAR_URI = stringPreferencesKey("avatar_uri")

    val SELECTED_SCHEME_URL = stringPreferencesKey("selected_scheme_url")
}

data class ProfileState(
    val name: String,
    val login: String,
    val email: String,
    val phone: String,
    val language: String,
    val avatarUri: String? = null
)

class UserPrefsRepository(private val context: Context) {

    val profileFlow: Flow<ProfileState> = context.dataStore.data.map { p ->
        ProfileState(
            name = p[PrefKeys.NAME] ?: "abober4000",
            login = p[PrefKeys.LOGIN] ?: "abober4000",
            email = p[PrefKeys.EMAIL] ?: "",
            phone = p[PrefKeys.PHONE] ?: "",
            language = p[PrefKeys.LANGUAGE] ?: "Русский",
            avatarUri = p[PrefKeys.AVATAR_URI]
        )
    }

    val selectedSchemeUrlFlow: Flow<String?> =
        context.dataStore.data.map { p -> p[PrefKeys.SELECTED_SCHEME_URL] }

    suspend fun saveProfile(state: ProfileState) {
        context.dataStore.edit { p ->
            p[PrefKeys.NAME] = state.name
            p[PrefKeys.LOGIN] = state.login
            p[PrefKeys.EMAIL] = state.email
            p[PrefKeys.PHONE] = state.phone
            p[PrefKeys.LANGUAGE] = state.language
            state.avatarUri?.let { p[PrefKeys.AVATAR_URI] = it } ?: p.remove(PrefKeys.AVATAR_URI)
        }
    }

    suspend fun setSelectedSchemeUrl(url: String) {
        context.dataStore.edit { p -> p[PrefKeys.SELECTED_SCHEME_URL] = url }
    }

    suspend fun clearSelectedSchemeUrl() {
        context.dataStore.edit { p -> p.remove(PrefKeys.SELECTED_SCHEME_URL) }
    }
}