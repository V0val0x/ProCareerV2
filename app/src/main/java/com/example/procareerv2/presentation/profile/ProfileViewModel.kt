package com.example.procareerv2.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.data.local.PreferencesManager
import com.example.procareerv2.domain.model.Interest
import com.example.procareerv2.domain.model.Skill
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
    val showEditSheet: Boolean = false,
    val isSkillsMode: Boolean = true,
    val showEditProfileDialog: Boolean = false,
    val skills: List<Skill> = emptyList(),
    val interests: List<Interest> = emptyList(),
    val editingSkill: Skill? = null,
    val editingInterest: Interest? = null
) {
    fun clearError() = copy(error = null)
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val defaultSkills = listOf(
        Skill(id = -1, name = "JavaScript", proficiencyLevel = 85),
        Skill(id = -2, name = "React", proficiencyLevel = 80),
        Skill(id = -3, name = "TypeScript", proficiencyLevel = 75),
        Skill(id = -4, name = "HTML/CSS", proficiencyLevel = 90)
    )

    private val defaultInterests = listOf(
        Interest(id = -1, name = "Web Development"),
        Interest(id = -2, name = "UI/UX Design"),
        Interest(id = -3, name = "Open Source"),
        Interest(id = -4, name = "Mobile Development")
    )

    private val defaultUser = User(
        id = -1,
        name = "Антон Петухов",
        email = "guest@example.com",
        token = "",
        position = "Front-end разработчик",
        profileImage = null,
        skills = defaultSkills,
        interests = defaultInterests
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
                        user = defaultUser,
                        skills = localUser?.skills ?: defaultSkills,
                        interests = localUser?.interests ?: defaultInterests
                    )}
                    // Save default user with skills and interests if no local data
                    if (localUser == null) {
                        preferencesManager.saveUser(defaultUser)
                    }
                } else {
                    // For logged in user, use their data
                    _uiState.update { it.copy(
                        user = currentUser,
                        skills = currentUser.skills,
                        interests = currentUser.interests
                    )}
                    // Save to local storage
                    preferencesManager.saveUser(currentUser)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun addSkill(skill: Skill) {
        viewModelScope.launch {
            preferencesManager.addSkill(skill)
            // Update UI state
            _uiState.update { currentState ->
                val updatedSkills = currentState.skills.toMutableList()
                if (!updatedSkills.any { it.name == skill.name }) {
                    updatedSkills.add(skill)
                }
                currentState.copy(skills = updatedSkills)
            }
        }
    }

    fun removeSkill(skillName: String) {
        viewModelScope.launch {
            preferencesManager.removeSkill(skillName)
            // Update UI state
            _uiState.update { currentState ->
                val updatedSkills = currentState.skills.filterNot { it.name == skillName }
                currentState.copy(skills = updatedSkills)
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

    fun showEditSheet(isSkillsMode: Boolean) {
        _uiState.update {
            it.copy(
                showEditSheet = true,
                isSkillsMode = isSkillsMode
            )
        }
    }

    fun showEditProfileDialog() {
        _uiState.update { it.copy(showEditProfileDialog = true) }
    }

    fun hideEditProfileDialog() {
        _uiState.update { it.copy(showEditProfileDialog = false, error = null) }
    }

    fun clearError() {
        _uiState.update { it.clearError() }
    }

    fun updateUserProfile(name: String, position: String, profileImage: String?) {
        viewModelScope.launch {
            val updatedUser = uiState.value.user?.copy(
                name = name,
                position = position,
                profileImage = profileImage,
                skills = uiState.value.skills,
                interests = uiState.value.interests
            )
            if (updatedUser != null) {
                authRepository.updateUserProfile(updatedUser)
                _uiState.update { it.copy(user = updatedUser) }
            }
        }
    }

    fun hideEditSheet() {
        _uiState.update { it.copy(
            showEditSheet = false,
            editingSkill = null,
            editingInterest = null
        )}
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
    
    fun startEditingSkill(skill: Skill) {
        _uiState.update { it.copy(editingSkill = skill) }
    }
    
    fun startEditingInterest(interest: Interest) {
        _uiState.update { it.copy(editingInterest = interest) }
    }

    fun addSkill(name: String, proficiencyLevel: Int) {
        viewModelScope.launch {
            val skills = uiState.value.skills.toMutableList()
            val editingSkill = uiState.value.editingSkill
            
            if (editingSkill != null) {
                // Update existing skill
                val updatedSkills = skills.map { skill ->
                    if (skill.id == editingSkill.id) {
                        skill.copy(name = name, proficiencyLevel = proficiencyLevel)
                    } else skill
                }
                val updatedUser = uiState.value.user?.copy(skills = updatedSkills)
                if (updatedUser != null) {
                    // Save to local storage first
                    preferencesManager.saveUser(updatedUser)
                    // Then update server if needed
                    try {
                        authRepository.updateUserSkills(updatedSkills)
                    } catch (e: Exception) {
                        println("Failed to update skills on server: ${e.message}")
                    }
                    // Update UI state
                    _uiState.update { it.copy(
                        skills = updatedSkills,
                        user = updatedUser,
                        editingSkill = null,
                        showEditSheet = false
                    )}
                }
            } else {
                // Add new skill
                val newSkill = Skill(
                    id = skills.size + 1,
                    name = name,
                    proficiencyLevel = proficiencyLevel
                )
                skills.add(newSkill)
                val updatedUser = uiState.value.user?.copy(skills = skills)
                if (updatedUser != null) {
                    // Save to local storage first
                    preferencesManager.saveUser(updatedUser)
                    // Then update server if needed
                    try {
                        authRepository.updateUserSkills(skills)
                    } catch (e: Exception) {
                        println("Failed to update skills on server: ${e.message}")
                    }
                    // Update UI state
                    _uiState.update { it.copy(
                        skills = skills,
                        user = updatedUser,
                        showEditSheet = false
                    )}
                }
            }
        }
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
                        showEditSheet = false
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
                        showEditSheet = false
                    )}
                }
            }
        }
    }

    fun deleteSkill(id: Int) {
        viewModelScope.launch {
            val updatedSkills = uiState.value.skills.filterNot { it.id == id }
            val updatedUser = uiState.value.user?.copy(skills = updatedSkills)
            if (updatedUser != null) {
                // Save to local storage first
                preferencesManager.saveUser(updatedUser)
                // Then update server if needed
                try {
                    authRepository.updateUserSkills(updatedSkills)
                } catch (e: Exception) {
                    println("Failed to update skills on server: ${e.message}")
                }
                // Update UI state
                _uiState.update { it.copy(
                    skills = updatedSkills,
                    user = updatedUser
                )}
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