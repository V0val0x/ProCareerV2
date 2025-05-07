package com.example.procareerv2.presentation.roadmap

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.domain.model.Roadmap
import com.example.procareerv2.domain.model.RoadmapStats
import com.example.procareerv2.domain.model.NodeStatus
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

data class RoadmapStatsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val roadmapStats: RoadmapStats? = null,
    val roadmap: Roadmap? = null
)

@HiltViewModel
class RoadmapStatsViewModel @Inject constructor(
    private val roadmapRepository: RoadmapRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoadmapStatsUiState(isLoading = true))
    val uiState: StateFlow<RoadmapStatsUiState> = _uiState.asStateFlow()
    
    init {
        loadRoadmapStats()
    }
    
    fun loadRoadmapStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val currentUser = authRepository.getUserFlow().first()
                
                if (currentUser != null) {
                    Log.d("RoadmapStatsViewModel", "Loading roadmap for user ${currentUser.id}")
                    roadmapRepository.getUserRoadmap(currentUser.id)
                        .onSuccess { roadmap ->
                            // Вычисляем статистику
                            var totalSkills = 0
                            var completedSkills = 0
                            
                            roadmap.themes.forEach { theme ->
                                theme.skills.forEach { skill ->
                                    totalSkills++
                                    if (skill.status == NodeStatus.LEARNED) {
                                        completedSkills++
                                    }
                                }
                            }
                            
                            val stats = RoadmapStats(
                                skillsLearned = completedSkills,
                                totalSkills = totalSkills,
                                testsCompleted = 7, // Это будет заменено реальными данными когда будет доступно
                                totalTests = 20     // Это будет заменено реальными данными когда будет доступно
                            )
                            
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    roadmapStats = stats,
                                    roadmap = roadmap,
                                    error = null
                                )
                            }
                        }
                        .onFailure { error ->
                            Log.e("RoadmapStatsViewModel", "Failed to load roadmap: ${error.message}")
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
                Log.e("RoadmapStatsViewModel", "Exception while loading roadmap: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }
}
