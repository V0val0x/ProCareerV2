package com.example.procareerv2.presentation.roadmap

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.domain.model.Roadmap
import com.example.procareerv2.domain.model.RoadmapSkill
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoadmapUiState(
    val roadmap: Roadmap? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RoadmapViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoadmapUiState())
    val uiState: StateFlow<RoadmapUiState> = _uiState.asStateFlow()

    init {
        val id = savedStateHandle.get<Int>("id") ?: 1
        loadRoadmap(id)
    }

    private fun loadRoadmap(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // В реальном приложении здесь будет запрос к репозиторию
                // Пока используем заглушку
                val mockRoadmap = getMockRoadmap(id)
                _uiState.update { it.copy(roadmap = mockRoadmap, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun getMockRoadmap(id: Int): Roadmap? {
        return when (id) {
            1 -> Roadmap(
                id = 1,
                title = "Frontend Developer",
                description = "Путь к становлению Frontend разработчиком",
                profession = "Разработка",
                skills = listOf(
                    RoadmapSkill(1, "HTML", 90, "Основа веб-разработки", listOf(2, 3)),
                    RoadmapSkill(2, "CSS", 85, "Стилизация веб-страниц", listOf(1, 3)),
                    RoadmapSkill(3, "JavaScript", 95, "Программирование на стороне клиента", listOf(1, 2)),
                    RoadmapSkill(4, "React", 80, "Библиотека для создания пользовательских интерфейсов", listOf(3)),
                    RoadmapSkill(5, "TypeScript", 75, "Типизированный JavaScript", listOf(3))
                )
            )
            2 -> Roadmap(
                id = 2,
                title = "Backend Developer",
                description = "Путь к становлению Backend разработчиком",
                profession = "Разработка",
                skills = listOf(
                    RoadmapSkill(1, "Java", 90, "Основной язык программирования", listOf(2, 3)),
                    RoadmapSkill(2, "Spring Framework", 85, "Фреймворк для создания веб-приложений", listOf(1)),
                    RoadmapSkill(3, "SQL", 80, "Язык запросов к базам данных", listOf(1)),
                    RoadmapSkill(4, "REST API", 75, "Архитектурный стиль взаимодействия компонентов", listOf(2))
                )
            )
            3 -> Roadmap(
                id = 3,
                title = "DevOps Engineer",
                description = "Путь к становлению DevOps инженером",
                profession = "Разработка",
                skills = listOf(
                    RoadmapSkill(1, "Linux", 90, "Операционная система", listOf(2, 3)),
                    RoadmapSkill(2, "Docker", 85, "Платформа контейнеризации", listOf(1, 3)),
                    RoadmapSkill(3, "Kubernetes", 80, "Система оркестрации контейнеров", listOf(1, 2))
                )
            )
            else -> null
        }
    }
}
