package com.example.procareerv2.presentation.roadmap

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.domain.model.Roadmap
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

data class RoadmapThemesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val roadmap: Roadmap? = null,
    val themes: List<RoadmapTheme> = emptyList()
)

@HiltViewModel
class RoadmapThemesViewModel @Inject constructor(
    private val roadmapRepository: RoadmapRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoadmapThemesUiState(isLoading = true))
    val uiState: StateFlow<RoadmapThemesUiState> = _uiState.asStateFlow()
    
    init {
        loadRoadmap()
    }
    
    fun loadRoadmap() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val currentUser = authRepository.getUserFlow().first()
                
                if (currentUser != null) {
                    Log.d("RoadmapThemesViewModel", "Loading roadmap for user ${currentUser.id}")
                    roadmapRepository.getUserRoadmap(currentUser.id)
                        .onSuccess { roadmap ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    roadmap = roadmap,
                                    themes = roadmap.themes.sortedBy { theme -> theme.position },
                                    error = null
                                )
                            }
                        }
                        .onFailure { error ->
                            Log.e("RoadmapThemesViewModel", "Failed to load roadmap: ${error.message}")
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
                Log.e("RoadmapThemesViewModel", "Exception while loading roadmap: ${e.message}")
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
