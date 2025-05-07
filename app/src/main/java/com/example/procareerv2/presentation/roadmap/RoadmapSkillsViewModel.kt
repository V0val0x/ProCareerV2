package com.example.procareerv2.presentation.roadmap

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.domain.model.NodeStatus
import com.example.procareerv2.domain.model.RoadmapSkill
import com.example.procareerv2.domain.model.RoadmapTheme
import com.example.procareerv2.domain.repository.AuthRepository
import com.example.procareerv2.domain.repository.RoadmapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoadmapSkillsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val theme: RoadmapTheme? = null,
    val skills: List<RoadmapSkill> = emptyList(),
    val showConfirmationDialog: Boolean = false,
    val selectedSkill: RoadmapSkill? = null,
    val updating: Boolean = false
)

@HiltViewModel
class RoadmapSkillsViewModel @Inject constructor(
    private val roadmapRepository: RoadmapRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val themeId: Int = checkNotNull(savedStateHandle.get<Int>("themeId"))
    
    private val _uiState = MutableStateFlow(RoadmapSkillsUiState(isLoading = true))
    val uiState: StateFlow<RoadmapSkillsUiState> = _uiState.asStateFlow()
    
    init {
        loadSkills()
    }
    
    fun loadSkills() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val currentUser = authRepository.getUserFlow().first()
                
                if (currentUser != null) {
                    Log.d("RoadmapSkillsViewModel", "Loading roadmap for user ${currentUser.id}")
                    roadmapRepository.getUserRoadmap(currentUser.id)
                        .onSuccess { roadmap ->
                            // Находим выбранную тему
                            val theme = roadmap.themes.find { it.id == themeId }
                            
                            if (theme != null) {
                                // Filter out skills with the same name as their parent theme
                                val filteredSkills = theme.skills.filter { skill ->
                                    !skill.name.equals(theme.title, ignoreCase = true)
                                }
                                
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false,
                                        theme = theme,
                                        skills = filteredSkills,
                                        error = null
                                    )
                                }
                            } else {
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false,
                                        error = "Тема не найдена"
                                    )
                                }
                            }
                        }
                        .onFailure { error ->
                            Log.e("RoadmapSkillsViewModel", "Failed to load roadmap: ${error.message}")
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = error.message ?: "Не удалось загрузить карту развития"
                                )
                            }
                        }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Пользователь не авторизован"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("RoadmapSkillsViewModel", "Exception while loading roadmap: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }
    
    fun toggleSkillStatus(skill: RoadmapSkill) {
        // Проверка, можно ли изменить статус скилла
        val newStatus = when (skill.status) {
            NodeStatus.NOT_STARTED -> NodeStatus.IN_PROGRESS
            NodeStatus.IN_PROGRESS -> NodeStatus.LEARNED
            NodeStatus.LEARNED -> {
                // Для перевода из LEARNED в NOT_STARTED нужно подтверждение
                _uiState.update { it.copy(showConfirmationDialog = true, selectedSkill = skill) }
                return
            }
        }
        
        updateSkillStatus(skill, newStatus)
    }
    
    fun confirmResetSkill() {
        val skill = uiState.value.selectedSkill ?: return
        updateSkillStatus(skill, NodeStatus.NOT_STARTED)
        dismissConfirmationDialog()
    }
    
    fun dismissConfirmationDialog() {
        _uiState.update { it.copy(showConfirmationDialog = false, selectedSkill = null) }
    }
    
    private fun updateSkillStatus(skill: RoadmapSkill, newStatus: NodeStatus) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(updating = true) }
                
                val currentUser = authRepository.getUserFlow().first()
                
                if (currentUser != null) {
                    roadmapRepository.updateNodeStatus(currentUser.id, skill.id, newStatus)
                        .onSuccess { 
                            // Обновляем локальное состояние
                            val updatedSkills = uiState.value.skills.map { 
                                if (it.id == skill.id) it.copy(status = newStatus) else it 
                            }
                            
                            _uiState.update { 
                                it.copy(
                                    updating = false,
                                    skills = updatedSkills,
                                    theme = it.theme?.copy(skills = updatedSkills)
                                )
                            }
                        }
                        .onFailure { error ->
                            Log.e("RoadmapSkillsViewModel", "Failed to update skill status: ${error.message}")
                            _uiState.update { it.copy(updating = false, error = "Не удалось обновить статус навыка") }
                        }
                } else {
                    _uiState.update { it.copy(updating = false, error = "Пользователь не авторизован") }
                }
            } catch (e: Exception) {
                Log.e("RoadmapSkillsViewModel", "Exception while updating skill status: ${e.message}")
                _uiState.update { it.copy(updating = false, error = e.message ?: "Неизвестная ошибка") }
            }
        }
    }
}
