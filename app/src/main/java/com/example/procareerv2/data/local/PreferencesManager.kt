package com.example.procareerv2.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.example.procareerv2.domain.model.Interest

import com.example.procareerv2.domain.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "procareer_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_TOKEN = stringPreferencesKey("user_token")
        private val USER_PROFILE_IMAGE = stringPreferencesKey("user_profile_image")
        private val USER_POSITION = stringPreferencesKey("user_position")
        private val USER_INTERESTS = stringPreferencesKey("user_interests")
        private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        private val gson = Gson()
    }

    fun getUserId(): String? = runBlocking {
        dataStore.data.first()[USER_ID]?.toString()
    }

    fun getAuthToken(): String? = runBlocking {
        dataStore.data.first()[USER_TOKEN]
    }

    suspend fun saveUser(user: User) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = user.id.toString()
            preferences[USER_NAME] = user.name
            preferences[USER_EMAIL] = user.email
            preferences[USER_TOKEN] = user.token
            user.profileImage?.let { preferences[USER_PROFILE_IMAGE] = it }
            user.position?.let { preferences[USER_POSITION] = it }
            preferences[USER_INTERESTS] = gson.toJson(user.interests)
        }
    }

    suspend fun updateInterests(interests: List<Interest>) {
        val currentUser = getUserFlow().first()
        currentUser?.let { user ->
            saveUser(user.copy(interests = interests))
        }
    }

    suspend fun addInterest(interest: Interest) {
        val currentUser = getUserFlow().first()
        currentUser?.let { user ->
            val updatedInterests = user.interests.toMutableList()
            if (!updatedInterests.any { it.name == interest.name }) {
                updatedInterests.add(interest)
                saveUser(user.copy(interests = updatedInterests))
            }
        }
    }

    suspend fun removeInterest(interestName: String) {
        val currentUser = getUserFlow().first()
        currentUser?.let { user ->
            val updatedInterests = user.interests.filterNot { it.name == interestName }
            saveUser(user.copy(interests = updatedInterests))
        }
    }

    fun getUserFlow(): Flow<User?> {
        return dataStore.data.map { preferences ->
            if (preferences[USER_ID] != null) {
                val interestsJson = preferences[USER_INTERESTS] ?: "[]"
                val interestsType = object : TypeToken<List<Interest>>() {}.type
                val interests = gson.fromJson<List<Interest>>(interestsJson, interestsType)

                // Get user ID from preferences
                val userId = preferences[USER_ID]?.toString()?.toIntOrNull() ?: -1

                User(
                    id = userId,
                    name = preferences[USER_NAME] ?: "",
                    email = preferences[USER_EMAIL] ?: "",
                    token = preferences[USER_TOKEN] ?: "",
                    profileImage = preferences[USER_PROFILE_IMAGE],
                    position = preferences[USER_POSITION],
                    interests = interests
                )
            } else {
                null
            }
        }
    }

    suspend fun getUser(): User? {
        var user: User? = null
        dataStore.data.map { preferences ->
            if (preferences[USER_ID] != null) {
                val interestsJson = preferences[USER_INTERESTS] ?: "[]"
                val interestsType = object : TypeToken<List<Interest>>() {}.type
                val interests = gson.fromJson<List<Interest>>(interestsJson, interestsType)

                // Get user ID and handle type conversion
                val userId = try {
                    preferences[USER_ID].toString().toInt()
                } catch (e: Exception) {
                    -1
                }

                user = User(
                    id = userId,
                    name = preferences[USER_NAME] ?: "",
                    email = preferences[USER_EMAIL] ?: "",
                    token = preferences[USER_TOKEN] ?: "",
                    profileImage = preferences[USER_PROFILE_IMAGE],
                    position = preferences[USER_POSITION],
                    interests = interests
                )
            }
        }.collect { }
        return user
    }

    suspend fun clearUser() {
        dataStore.edit { preferences ->
            preferences.clear() // Очищаем все данные пользователя
        }
    }

    // Add this method to check if it's the first launch
    suspend fun isFirstLaunch(): Boolean {
        val preferences = dataStore.data.first()  // Получаем данные асинхронно
        val isFirst = preferences[IS_FIRST_LAUNCH] ?: true

        // Сразу обновляем значение для следующего запуска
        dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = false
        }

        return isFirst
    }

    // Optional: Add a method to reset the first launch state (useful for testing)
    suspend fun resetFirstLaunch() {
        dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = true
        }
    }
}