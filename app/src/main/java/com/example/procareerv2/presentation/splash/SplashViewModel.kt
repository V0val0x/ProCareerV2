package com.example.procareerv2.presentation.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.data.local.PreferencesManager
import com.example.procareerv2.data.local.UserPreferencesManager
import com.example.procareerv2.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthentication()
    }

    private fun checkAuthentication() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Проверяем, первый ли это запуск
                val isFirst = preferencesManager.isFirstLaunch()
                if (isFirst) {
                    _authState.value = AuthState.FirstLaunch
                    return@launch
                }
                
                // Проверяем, авторизован ли пользователь уже
                val isAlreadyLoggedIn = authRepository.isLoggedIn()
                if (isAlreadyLoggedIn) {
                    Log.d("SplashViewModel", "User is already logged in")
                    _authState.value = AuthState.Authenticated
                    return@launch
                }
                
                // Проверяем наличие сохраненных учетных данных
                val credentials = userPreferencesManager.getSavedCredentials().first()
                Log.d("SplashViewModel", "Saved credentials check: email=${credentials.email}, rememberMe=${credentials.rememberMe}")
                
                if (credentials.rememberMe && credentials.email.isNotBlank() && credentials.password.isNotBlank()) {
                    // Если есть сохраненные данные, пытаемся выполнить вход
                    Log.d("SplashViewModel", "Attempting auto-login with saved credentials: ${credentials.email}")
                    
                    val loginResult = authRepository.login(credentials.email, credentials.password)
                    if (loginResult.isSuccess) {
                        Log.d("SplashViewModel", "Auto-login successful!")
                        // Важно: сохраняем флаг "rememberMe", чтобы он не потерялся при входе
                        userPreferencesManager.saveCredentials(credentials.email, credentials.password, true)
                        _authState.value = AuthState.Authenticated
                    } else {
                        Log.e("SplashViewModel", "Auto-login failed: ${loginResult.exceptionOrNull()?.message}")
                        // Если автоматический вход не удался, возможно, учетные данные устарели
                        _authState.value = AuthState.Unauthenticated
                    }
                } else {
                    Log.d("SplashViewModel", "No valid saved credentials for auto-login")
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                Log.e("SplashViewModel", "Error during authentication check: ${e.message}", e)
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    suspend fun isFirstLaunch(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                preferencesManager.isFirstLaunch()
            } catch (e: Exception) {
                Log.e("SplashViewModel", "Error checking first launch: ${e.message}", e)
                false
            }
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return withContext(Dispatchers.IO) {
            _authState.value == AuthState.Authenticated
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object FirstLaunch : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}