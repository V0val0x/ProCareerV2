package com.example.procareerv2.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_PASSWORD = stringPreferencesKey("password")
        private val KEY_REMEMBER_ME = booleanPreferencesKey("remember_me")
    }

    suspend fun saveCredentials(email: String, password: String, rememberMe: Boolean) {
        dataStore.edit { preferences ->
            if (rememberMe) {
                preferences[KEY_EMAIL] = email
                preferences[KEY_PASSWORD] = password
            } else {
                preferences.remove(KEY_EMAIL)
                preferences.remove(KEY_PASSWORD)
            }
            preferences[KEY_REMEMBER_ME] = rememberMe
        }
    }

    fun getSavedCredentials(): Flow<SavedCredentials> = dataStore.data.map { preferences ->
        SavedCredentials(
            email = preferences[KEY_EMAIL] ?: "",
            password = preferences[KEY_PASSWORD] ?: "",
            rememberMe = preferences[KEY_REMEMBER_ME] ?: false
        )
    }

    fun hasValidCredentials(): Flow<Boolean> = dataStore.data.map { preferences ->
        val email = preferences[KEY_EMAIL] ?: ""
        val password = preferences[KEY_PASSWORD] ?: ""
        val rememberMe = preferences[KEY_REMEMBER_ME] ?: false
        rememberMe && email.isNotBlank() && password.isNotBlank()
    }

    suspend fun clearCredentials() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_EMAIL)
            preferences.remove(KEY_PASSWORD)
            preferences.remove(KEY_REMEMBER_ME)
        }
    }
}

data class SavedCredentials(
    val email: String,
    val password: String,
    val rememberMe: Boolean
)
