package com.example.procareerv2.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        Log.d("UserPreferences", "Saving credentials: email=$email, rememberMe=$rememberMe")
        try {
            withContext(NonCancellable) {
                dataStore.edit { preferences ->
                    if (rememberMe) {
                        preferences[KEY_EMAIL] = email
                        preferences[KEY_PASSWORD] = password
                        preferences[KEY_REMEMBER_ME] = true
                        Log.d("UserPreferences", "Credentials saved with rememberMe=true")
                    } else {
                        preferences.remove(KEY_EMAIL)
                        preferences.remove(KEY_PASSWORD)
                        preferences[KEY_REMEMBER_ME] = false
                        Log.d("UserPreferences", "Credentials removed because rememberMe=false")
                    }
                }
            }
            
            // Проверяем, что данные действительно сохранились
            val savedCredentials = getSavedCredentials().first()
            Log.d("UserPreferences", "Verification after save - credentials in storage: email=${savedCredentials.email}, rememberMe=${savedCredentials.rememberMe}")
        } catch (e: Exception) {
            Log.e("UserPreferences", "Error saving credentials: ${e.message}", e)
        }
    }

    fun getSavedCredentials(): Flow<SavedCredentials> = dataStore.data.map { preferences ->
        val email = preferences[KEY_EMAIL] ?: ""
        val password = preferences[KEY_PASSWORD] ?: ""
        val rememberMe = preferences[KEY_REMEMBER_ME] ?: false
        Log.d("UserPreferences", "Getting saved credentials: email=$email, rememberMe=$rememberMe")
        SavedCredentials(
            email = email,
            password = password,
            rememberMe = rememberMe
        )
    }

    fun hasValidCredentials(): Flow<Boolean> = dataStore.data.map { preferences ->
        val email = preferences[KEY_EMAIL] ?: ""
        val password = preferences[KEY_PASSWORD] ?: ""
        val rememberMe = preferences[KEY_REMEMBER_ME] ?: false
        val isValid = rememberMe && email.isNotBlank() && password.isNotBlank()
        Log.d("UserPreferences", "Checking valid credentials: email=$email, rememberMe=$rememberMe, isValid=$isValid")
        isValid
    }
    
    /**
     * Очищает сохраненные учетные данные пользователя
     * Вызывается при выходе из аккаунта для предотвращения автоматического входа
     */
    suspend fun clearCredentials() {
        Log.d("UserPreferences", "Clearing saved credentials")
        try {
            withContext(NonCancellable) {
                dataStore.edit { preferences ->
                    preferences.remove(KEY_EMAIL)
                    preferences.remove(KEY_PASSWORD)
                    preferences[KEY_REMEMBER_ME] = false
                }
            }
            Log.d("UserPreferences", "Credentials cleared successfully")
        } catch (e: Exception) {
            Log.e("UserPreferences", "Error clearing credentials: ${e.message}", e)
        }
    }
}

data class SavedCredentials(
    val email: String,
    val password: String,
    val rememberMe: Boolean
)
