package com.example.procareerv2.presentation.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.data.local.PreferencesManager
import com.example.procareerv2.data.local.UserPreferencesManager
import com.example.procareerv2.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
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
    private val userPreferencesManager: UserPreferencesManager,
    private val externalScope: CoroutineScope
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthentication()
    }

    private fun checkAuthentication() {
        // Start a timer to force transition after max timeout no matter what
        viewModelScope.launch {
            // Force transition after 3 seconds, regardless of what happens
            kotlinx.coroutines.delay(3000) 
            if (_authState.value == AuthState.Loading) {
                Log.e("SplashViewModel", "Forcing transition due to timeout")
                _authState.value = AuthState.Unauthenticated
            }
        }
        
        // Use a separate coroutine for actual authentication
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // First quickly check if this is the first launch
                val isFirst = preferencesManager.isFirstLaunch()
                if (isFirst) {
                    Log.d("SplashViewModel", "This is the first app launch")
                    _authState.value = AuthState.FirstLaunch
                    return@launch
                }
                
                // Проверяем сохраненные учетные данные
                val credentials = try {
                    userPreferencesManager.getSavedCredentials().first()
                } catch (e: Exception) {
                    Log.e("SplashViewModel", "Error getting saved credentials: ${e.message}")
                    null
                }
                
                if (credentials != null && credentials.rememberMe && 
                    credentials.email.isNotBlank() && credentials.password.isNotBlank()) {
                    
                    Log.d("SplashViewModel", "Attempting auto-login with saved credentials")
                    try {
                        val loginResult = authRepository.login(credentials.email, credentials.password) 
                        if (loginResult.isSuccess) {
                            Log.d("SplashViewModel", "Auto-login successful!")
                            _authState.value = AuthState.Authenticated
                        } else {
                            Log.e("SplashViewModel", "Auto-login failed")
                            _authState.value = AuthState.Unauthenticated
                        }
                    } catch (e: Exception) {
                        Log.e("SplashViewModel", "Auto-login exception: ${e.message}")
                        _authState.value = AuthState.Unauthenticated
                    }
                } else {
                    Log.d("SplashViewModel", "No valid saved credentials, user needs to login")
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                Log.e("SplashViewModel", "Authentication check failed: ${e.message}")
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