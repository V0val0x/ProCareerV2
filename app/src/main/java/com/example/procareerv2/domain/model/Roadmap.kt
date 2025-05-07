package com.example.procareerv2.domain.model

// Основная модель роадмапа
data class Roadmap(
    val id: Int,
    val title: String,
    val description: String,
    val profession: String,
    val themes: List<RoadmapTheme>, // Теперь у нас есть темы внутри роадмапа
    val totalNodes: Int = 0,
    val completedNodes: Int = 0
)

// Модель для представления темы в роадмапе
data class RoadmapTheme(
    val id: Int,
    val title: String,
    val description: String,
    val skills: List<RoadmapSkill>, // Каждая тема содержит набор навыков
    val position: Int = 0 // Позиция в списке тем
)

// Модель для представления навыка в роадмапе
data class RoadmapSkill(
    val id: Int,
    val name: String,
    val importance: Int, // 1-100
    val description: String,
    val status: NodeStatus = NodeStatus.NOT_STARTED, // Статус изучения навыка
    val relatedSkills: List<Int> = emptyList() // IDs of related skills
)

// Возможные статусы для навыка
enum class NodeStatus {
    NOT_STARTED, // Не начато
    IN_PROGRESS,  // Изменено с STARTED на IN_PROGRESS для соответствия API
    LEARNED      // Изменено с FINISHED на LEARNED для соответствия API
}

// Класс для передачи запроса на обновление статуса навыка
data class UpdateNodeStatusRequest(
    val node_id: Int,
    val status: String
)

// Класс для хранения статистики роадмапа
data class RoadmapStats(
    val skillsLearned: Int,
    val totalSkills: Int,
    val testsCompleted: Int,
    val totalTests: Int
) {
    val progressPercentage: Int
        get() = if (totalSkills > 0) (skillsLearned * 100) / totalSkills else 0
}
