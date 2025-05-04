package com.example.procareerv2.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.data.local.PreferencesManager
import com.example.procareerv2.data.local.UserPreferencesManager
import com.example.procareerv2.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    suspend fun isFirstLaunch(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                preferencesManager.isFirstLaunch()
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Сначала проверяем наличие сохраненных учетных данных
                val hasValidCredentials = userPreferencesManager.hasValidCredentials().firstOrNull() ?: false
                if (hasValidCredentials) {
                    // Если есть сохраненные данные, пытаемся выполнить вход
                    val credentials = userPreferencesManager.getSavedCredentials().first()
                    authRepository.login(credentials.email, credentials.password)
                        .isSuccess
                } else {
                    // Если нет сохраненных данных, проверяем текущий статус входа
                    authRepository.isLoggedIn()
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}