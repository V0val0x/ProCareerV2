package com.example.procareerv2.presentation.roadmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.domain.model.Roadmap
import com.example.procareerv2.domain.model.RoadmapSkill
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoadmapListUiState(
    val roadmaps: List<Roadmap> = emptyList(),
    val professions: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RoadmapListViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RoadmapListUiState())
    val uiState: StateFlow<RoadmapListUiState> = _uiState

    init {
        loadRoadmaps()
    }

    private fun loadRoadmaps() {
        viewModelScope.launch {
            // Заглушка с тестовыми данными
            val mockRoadmaps = listOf(
                Roadmap(
                    id = 1,
                    title = "Frontend Developer",
                    description = "Путь к становлению Frontend разработчиком",
                    profession = "Разработка",
                    skills = listOf(
                        RoadmapSkill(1, "HTML", 90, "Основа веб-разработки", listOf(2, 3)),
                        RoadmapSkill(2, "CSS", 85, "Стилизация веб-страниц", listOf(1, 3)),
                        RoadmapSkill(3, "JavaScript", 95, "Программирование на стороне клиента", listOf(1, 2)),
                        RoadmapSkill(4, "React", 80, "Популярный фреймворк", listOf(3)),
                        RoadmapSkill(5, "TypeScript", 75, "Типизированный JavaScript", listOf(3, 4))
                    )
                ),
                Roadmap(
                    id = 2,
                    title = "Backend Developer",
                    description = "Путь к становлению Backend разработчиком",
                    profession = "Разработка",
                    skills = listOf(
                        RoadmapSkill(1, "Java", 90, "Основной язык программирования", listOf(2)),
                        RoadmapSkill(2, "Spring", 85, "Фреймворк для разработки", listOf(1)),
                        RoadmapSkill(3, "SQL", 80, "Работа с базами данных", listOf(4)),
                        RoadmapSkill(4, "REST API", 85, "Проектирование API", listOf(1, 2))
                    )
                ),
                Roadmap(
                    id = 3,
                    title = "UI/UX Designer",
                    description = "Путь к становлению UI/UX дизайнером",
                    profession = "Дизайн",
                    skills = listOf(
                        RoadmapSkill(1, "Figma", 90, "Инструмент для дизайна", listOf(2)),
                        RoadmapSkill(2, "UI Design", 85, "Дизайн интерфейсов", listOf(1, 3)),
                        RoadmapSkill(3, "UX Research", 80, "Исследование пользователей", listOf(2))
                    )
                )
            )

            val professions = mockRoadmaps.map { it.profession }.distinct()

            _uiState.update { it.copy(
                roadmaps = mockRoadmaps,
                professions = professions
            ) }
        }
    }

    fun searchRoadmaps(query: String, profession: String?) {
        viewModelScope.launch {
            val filteredRoadmaps = uiState.value.roadmaps.filter { roadmap ->
                val matchesQuery = query.isEmpty() || 
                    roadmap.title.contains(query, ignoreCase = true) ||
                    roadmap.description.contains(query, ignoreCase = true)
                val matchesProfession = profession == null || roadmap.profession == profession
                matchesQuery && matchesProfession
            }
            _uiState.update { it.copy(roadmaps = filteredRoadmaps) }
        }
    }
}
