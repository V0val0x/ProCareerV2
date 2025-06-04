package com.example.procareerv2.presentation.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.data.local.PreferencesManager
import com.example.procareerv2.data.local.UserPreferencesManager
import com.example.procareerv2.domain.model.Interest
import dagger.hilt.android.qualifiers.ApplicationContext

import com.example.procareerv2.domain.model.User
import com.example.procareerv2.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val interests: List<Interest> = emptyList(),
    val showEditInterestsSheet: Boolean = false,
    val showEditProfileDialog: Boolean = false,
    val editingInterest: Interest? = null
) {
    fun clearError() = copy(error = null)
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
    private val userPreferencesManager: UserPreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val defaultUser = User(
        id = -1,
        name = "Антон Петухов",
        email = "example@mail.com",
        token = "",
        position = "Front-end разработчик",
        specialization = "",
        profileImage = null,
        interests = emptyList()
    )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Загружаем начальные данные из DataStore
            val storedUser = preferencesManager.getUserFlow().first()
            if (storedUser != null) {
                // Проверяем, есть ли аватар для текущего пользователя в карте аватаров
                val userAvatar = preferencesManager.getUserAvatar(storedUser.id)
                val userWithAvatar = if (userAvatar != null && (storedUser.profileImage == null || storedUser.profileImage.isBlank())) {
                    Log.d("ProfileViewModel", "Found avatar for user ${storedUser.id}: $userAvatar")
                    storedUser.copy(profileImage = userAvatar)
                } else {
                    storedUser
                }
                
                _uiState.update { it.copy(user = userWithAvatar, interests = userWithAvatar.interests) }
                // Immediately fetch updated profile data from server
                fetchUserProfile(userWithAvatar.id)
            } else {
                Log.d("ProfileViewModel", "No stored user found, using default")
                _uiState.update { it.copy(user = defaultUser) }
            }
        }
    }
    
    // Публичный метод для обновления профиля, который можно вызвать из UI
    fun refreshUserProfile() {
        viewModelScope.launch {
            // Проверяем, авторизован ли пользователь
            val currentUser = authRepository.getUserFlow().first()
            if (currentUser != null) {
                Log.d("ProfileViewModel", "Refreshing user profile for user ID: ${currentUser.id}")
                fetchUserProfile(currentUser.id)
            } else {
                Log.d("ProfileViewModel", "No logged in user found, using default")
                _uiState.update { it.copy(user = defaultUser) }
            }
        }
    }

    private fun fetchUserProfile(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                Log.d("ProfileViewModel", "Fetching user profile from server for ID: $userId")
                
                // Сначала проверяем, есть ли сохраненный аватар для данного пользователя
                val savedAvatar = preferencesManager.getUserAvatar(userId)
                Log.d("ProfileViewModel", "Saved avatar for user $userId: $savedAvatar")
                
                val result = authRepository.fetchUserProfile(userId)
                result.onSuccess { user ->
                    Log.d("ProfileViewModel", "Profile fetched successfully: ${user.name}")
                    
                    // Применяем сохраненный аватар, если он есть и сервер не вернул аватар
                    val updatedUser = if (savedAvatar != null && (user.profileImage == null || user.profileImage.isBlank())) {
                        Log.d("ProfileViewModel", "Applying saved avatar to user ${user.id}: $savedAvatar")
                        user.copy(profileImage = savedAvatar)
                    } else {
                        if (user.profileImage != null) {
                            // Если сервер вернул аватар, сохраняем его в карту
                            preferencesManager.saveUserAvatar(user.id, user.profileImage)
                            Log.d("ProfileViewModel", "Saved new avatar from server for user ${user.id}: ${user.profileImage}")
                        }
                        user
                    }
                    
                    // Проверяем, есть ли специализация. Если нет, используем текущую из UI
                    val currentState = _uiState.value
                    val finalUser = if (updatedUser.specialization.isNullOrBlank() && 
                                      currentState.user?.specialization?.isNotBlank() == true) {
                        Log.d("ProfileViewModel", "Keeping current specialization: ${currentState.user?.specialization}")
                        updatedUser.copy(specialization = currentState.user?.specialization)
                    } else {
                        updatedUser
                    }
                    
                    _uiState.update { it.copy(
                        user = finalUser,
                        interests = finalUser.interests,
                        isLoading = false,
                        error = null
                    )}
                    
                    // Сохраняем обновленные данные в DataStore
                    preferencesManager.saveUser(finalUser)
                }.onFailure { error ->
                    Log.e("ProfileViewModel", "Failed to fetch profile: ${error.message}")
                    _uiState.update { it.copy(
                        error = error.message ?: "Не удалось загрузить профиль",
                        isLoading = false
                    )}
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Exception while fetching profile: ${e.message}")
                _uiState.update { it.copy(
                    error = e.message ?: "Не удалось загрузить профиль",
                    isLoading = false
                )}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                // Очищаем сохранённые учетные данные
                userPreferencesManager.clearCredentials()
                // Выполняем выход через репозиторий
                authRepository.logout()
                // Очищаем состояние UI
                _uiState.update { 
                    ProfileUiState() // Сбрасываем все состояние к начальному
                }
                Log.d("ProfileViewModel", "Logout completed successfully")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error during logout: ${e.message}")
                _uiState.update { it.copy(error = "Ошибка при выходе из аккаунта") }
            }
        }
    }

    fun addInterest(interest: Interest) {
        viewModelScope.launch {
            preferencesManager.addInterest(interest)
            // Update UI state
            _uiState.update { currentState ->
                val updatedInterests = currentState.interests.toMutableList()
                if (!updatedInterests.any { it.name == interest.name }) {
                    updatedInterests.add(interest)
                }
                currentState.copy(interests = updatedInterests)
            }
        }
    }

    fun removeInterest(interestName: String) {
        viewModelScope.launch {
            preferencesManager.removeInterest(interestName)
            // Update UI state
            _uiState.update { currentState ->
                val updatedInterests = currentState.interests.filterNot { it.name == interestName }
                currentState.copy(interests = updatedInterests)
            }
        }
    }

    fun showEditProfileDialog() {
        _uiState.update { it.copy(showEditInterestsSheet = false, showEditProfileDialog = true) }
    }

    fun hideEditProfileDialog() {
        _uiState.update { it.copy(showEditInterestsSheet = false, showEditProfileDialog = false, error = null) }
    }

    fun clearError() {
        _uiState.update { it.clearError() }
    }

    fun updateUserProfile(name: String, position: String, specialization: String, profileImage: Uri?) {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Updating profile: name=$name, position=$position, specialization=$specialization, imageUri=$profileImage")
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val currentUser = uiState.value.user ?: throw Exception("Пользователь не найден")
                Log.d("ProfileViewModel", "Current user: ${currentUser.id}, ${currentUser.name}")
                
                // Обрабатываем изображение профиля, если оно было изменено
                var profileImagePath = currentUser.profileImage
                if (profileImage != null) {
                    // Сохраняем новое изображение
                    profileImagePath = com.example.procareerv2.util.ImageUtils.saveImageToInternalStorage(context, profileImage)
                    
                    // Сохраняем в карте аватаров по ID пользователя
                    preferencesManager.saveUserAvatar(currentUser.id, profileImagePath)
                    Log.d("ProfileViewModel", "Saved profile image for user ${currentUser.id}: $profileImagePath")
                } else {
                    // Пытаемся загрузить аватар из карты аватаров
                    val savedAvatar = preferencesManager.getUserAvatar(currentUser.id)
                    if (savedAvatar != null && (profileImagePath == null || profileImagePath.isBlank())) {
                        profileImagePath = savedAvatar
                        Log.d("ProfileViewModel", "Loaded saved avatar for user ${currentUser.id}: $profileImagePath")
                    }
                }
                
                // Создаем обновленного пользователя с новыми данными
                val updatedUser = currentUser.copy(
                    name = name,
                    position = position.lowercase(),
                    specialization = specialization.lowercase(),
                    profileImage = profileImagePath
                )
                
                Log.d("ProfileViewModel", "Sending update to repository for user ID: ${updatedUser.id}")
                val result = authRepository.updateUserProfile(updatedUser)
                
                result.onSuccess { user ->
                    // Сохраняем изображение профиля в обновленном пользователе
                    val userWithImage = if (profileImagePath != null && user.profileImage == null) {
                        // Если сервер не вернул изображение, используем локальное
                        user.copy(profileImage = profileImagePath)
                    } else {
                        user
                    }
                    
                    Log.d("ProfileViewModel", "Profile updated successfully: ${userWithImage.name}")
                    _uiState.update { it.copy(
                        user = userWithImage,
                        interests = userWithImage.interests,
                        isLoading = false,
                        error = null,
                        showEditProfileDialog = false
                    )}
                    
                    // Сохраняем обновленные данные в локальное хранилище
                    preferencesManager.saveUser(userWithImage)
                    
                    // Обновляем данные с сервера после успешного обновления профиля
                    refreshUserProfile()
                }.onFailure { error ->
                    Log.e("ProfileViewModel", "Failed to update profile: ${error.message}")
                    _uiState.update { it.copy(
                        error = error.message ?: "Не удалось обновить профиль",
                        isLoading = false
                    )}
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Exception during profile update: ${e.message}")
                _uiState.update { it.copy(
                    error = e.message ?: "Не удалось обновить профиль",
                    isLoading = false
                )}
            }
        }
    }

    fun showEditSheet(isSkillsMode: Boolean) {
        _uiState.update { it.copy(showEditInterestsSheet = true) }
    }

    fun hideEditSheet() {
        _uiState.update { it.copy(showEditInterestsSheet = false, editingInterest = null) }
    }

    fun updateProfileImage(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                // Get current user
                val currentUser = _uiState.value.user ?: return@launch
                
                // Update user with new image path
                val updatedUser = currentUser.copy(
                    profileImage = uri.toString()
                )
                
                // Save to local storage
                preferencesManager.saveUser(updatedUser)
                
                // Update UI state
                _uiState.update { state ->
                    state.copy(
                        user = updatedUser,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Ошибка при обновлении изображения: ${e.message}",
                    isLoading = false
                )}
            }
        }
    }

    fun startEditingInterest(interest: Interest) {
        _uiState.update { it.copy(editingInterest = interest) }
    }

    fun addInterest(name: String) {
        viewModelScope.launch {
            val interests = uiState.value.interests.toMutableList()
            val editingInterest = uiState.value.editingInterest
            
            if (editingInterest != null) {
                // Update existing interest
                val updatedInterests = interests.map { interest ->
                    if (interest.id == editingInterest.id) {
                        interest.copy(name = name)
                    } else interest
                }
                val updatedUser = uiState.value.user?.copy(interests = updatedInterests)
                if (updatedUser != null) {
                    // Save to local storage first
                    preferencesManager.saveUser(updatedUser)
                    // Then update server if needed
                    try {
                        authRepository.updateUserInterests(updatedInterests)
                    } catch (e: Exception) {
                        println("Failed to update interests on server: ${e.message}")
                    }
                    // Update UI state
                    _uiState.update { it.copy(
                        interests = updatedInterests,
                        user = updatedUser,
                        editingInterest = null,
                        showEditInterestsSheet = false
                    )}
                }
            } else {
                // Add new interest
                val newInterest = Interest(
                    id = interests.size + 1,
                    name = name
                )
                interests.add(newInterest)
                val updatedUser = uiState.value.user?.copy(interests = interests)
                if (updatedUser != null) {
                    // Save to local storage first
                    preferencesManager.saveUser(updatedUser)
                    // Then update server if needed
                    try {
                        authRepository.updateUserInterests(interests)
                    } catch (e: Exception) {
                        println("Failed to update interests on server: ${e.message}")
                    }
                    // Update UI state
                    _uiState.update { it.copy(
                        interests = interests,
                        user = updatedUser,
                        showEditInterestsSheet = false
                    )}
                }
            }
        }
    }

    // Удаление интереса по имени вместо ID
    fun deleteInterest(name: String) {
        viewModelScope.launch {
            android.util.Log.d("ProfileViewModel", "Удаление интереса по имени: $name")
            val updatedInterests = uiState.value.interests.filterNot { it.name == name }
            val updatedUser = uiState.value.user?.copy(interests = updatedInterests)
            if (updatedUser != null) {
                // Save to local storage first
                preferencesManager.saveUser(updatedUser)
                // Then update server if needed
                try {
                    authRepository.updateUserInterests(updatedInterests)
                } catch (e: Exception) {
                    println("Failed to update interests on server: ${e.message}")
                }
                // Update UI state
                _uiState.update { it.copy(
                    interests = updatedInterests,
                    user = updatedUser
                )}
            }
        }
    }
}