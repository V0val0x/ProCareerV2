package com.example.procareerv2.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.data.local.PreferencesManager
import com.example.procareerv2.domain.model.Interest

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
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val defaultUser = User(
        id = -1,
        name = "Антон Петухов",
        email = "example@mail.com",
        token = "",
        position = "Front-end разработчик",
        profileImage = null,
        interests = emptyList()
    )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Try to get current user, if not available, use default user
        viewModelScope.launch {
            // Combine flows from auth repository and preferences manager
            authRepository.getUserFlow().collect { currentUser ->
                if (currentUser?.id == 1 || currentUser == null) {
                    // For default/guest user, try to get locally stored skills and interests
                    val localUser = preferencesManager.getUserFlow().first()
                    _uiState.update { it.copy(
                        user = defaultUser
                    )}
                    // Save default user with skills and interests if no local data
                    if (localUser == null) {
                        preferencesManager.saveUser(defaultUser)
                    }
                } else {
                    // For logged in user, fetch their profile data
                    fetchUserProfile(currentUser.id)
                }
            }
        }
    }

    private fun fetchUserProfile(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = authRepository.fetchUserProfile(userId)
                result.onSuccess { user ->
                    _uiState.update { it.copy(
                        user = user,
                        interests = user.interests,
                        isLoading = false
                    )}
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message ?: "Failed to fetch user profile", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to fetch user profile", isLoading = false) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
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

    fun updateUserProfile(name: String, position: String, profileImage: String?) {
        viewModelScope.launch {
            val updatedUser = uiState.value.user?.copy(
                name = name,
                position = position,
                profileImage = profileImage
            )
            if (updatedUser != null) {
                authRepository.updateUserProfile(updatedUser)
                _uiState.update { it.copy(user = updatedUser) }
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

    fun deleteInterest(id: Int) {
        viewModelScope.launch {
            val updatedInterests = uiState.value.interests.filterNot { it.id == id }
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