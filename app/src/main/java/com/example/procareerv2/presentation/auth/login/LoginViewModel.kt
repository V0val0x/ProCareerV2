package com.example.procareerv2.presentation.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.data.local.UserPreferencesManager
import com.example.procareerv2.domain.model.User
import com.example.procareerv2.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val externalScope: CoroutineScope
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesManager.getSavedCredentials().collect { credentials ->
                if (credentials.rememberMe) {
                    _uiState.update {
                        it.copy(
                            email = credentials.email,
                            password = credentials.password,
                            rememberMe = true
                        )
                    }
                }
            }
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onRememberMeChanged(rememberMe: Boolean) {
        _uiState.update { it.copy(rememberMe = rememberMe) }
    }

    fun login() {
        if (!validateInputs(_uiState.value.email, _uiState.value.password)) return

        _uiState.update { it.copy(isLoading = true, error = null) }
        Log.d("LoginViewModel", "Starting login process")

        viewModelScope.launch {
            try {
                val result = authRepository.login(_uiState.value.email, _uiState.value.password)
                result.onSuccess { user ->
                    Log.d("LoginViewModel", "Login successful: ${user.id}")
                    // Save credentials in the external scope with NonCancellable context
                    externalScope.launch(Dispatchers.IO + NonCancellable) {
                        try {
                            userPreferencesManager.saveCredentials(
                                _uiState.value.email,
                                _uiState.value.password,
                                _uiState.value.rememberMe
                            )
                            Log.d("LoginViewModel", "Credentials saved successfully, rememberMe: ${_uiState.value.rememberMe}")
                        } catch (e: Exception) {
                            Log.e("LoginViewModel", "Failed to save credentials: ${e.message}")
                        }
                    }
                    
                    // Update UI state
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    Log.d("LoginViewModel", "Updated state - isLoggedIn: ${_uiState.value.isLoggedIn}")
                }
                .onFailure { exception ->
                    Log.e("LoginViewModel", "Login failed: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Ошибка входа"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Exception during login: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Непредвиденная ошибка входа"
                    )
                }
            }
        }
    }

    // Прямой метод входа, возвращающий Result<User> для обработки в UI
    suspend fun loginDirect(email: String, password: String): Result<User> {
        if (!validateInputs(email, password)) return Result.failure(Exception("Ошибка валидации"))

        _uiState.update { it.copy(isLoading = true, error = null) }
        Log.d("LoginViewModel", "Starting direct login process")

        return try {
            val result = authRepository.login(email, password)
            
            result.onSuccess { user ->
                Log.d("LoginViewModel", "Direct login successful: ${user.id}")
                // Save credentials in the external scope with NonCancellable context
                externalScope.launch(Dispatchers.IO + NonCancellable) {
                    try {
                        val rememberMe = _uiState.value.rememberMe
                        Log.d("LoginViewModel", "Saving credentials with rememberMe=$rememberMe")
                        userPreferencesManager.saveCredentials(
                            email,
                            password,
                            rememberMe
                        )
                        Log.d("LoginViewModel", "Credentials saved successfully")
                    } catch (e: Exception) {
                        Log.e("LoginViewModel", "Error saving credentials: ${e.message}")
                    }
                }
                
                // Update UI state
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                Log.d("LoginViewModel", "Updated state after direct login - isLoggedIn: ${_uiState.value.isLoggedIn}")
            }
            
            result.onFailure { exception ->
                Log.e("LoginViewModel", "Direct login failed: ${exception.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Ошибка входа"
                    )
                }
            }
            
            result
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Exception during direct login: ${e.message}")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = e.message ?: "Непредвиденная ошибка входа"
                )
            }
            Result.failure(e)
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email не может быть пустым") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Некорректный email") }
            isValid = false
        }

        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Пароль не может быть пустым") }
            isValid = false
        } else if (password.length < 6) {
            _uiState.update { it.copy(passwordError = "Пароль должен содержать минимум 6 символов") }
            isValid = false
        }

        return isValid
    }
}