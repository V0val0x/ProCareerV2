package com.example.procareerv2.data.remote.dto

import com.example.procareerv2.domain.model.NodeStatus
import com.example.procareerv2.domain.model.Roadmap
import com.example.procareerv2.domain.model.RoadmapSkill
import com.example.procareerv2.domain.model.RoadmapTheme
import com.google.gson.annotations.SerializedName

data class RoadmapResponse(
    @SerializedName("specialization") val specialization: String = "",
    @SerializedName("grade") val grade: String = "",
    @SerializedName("skills") val skills: List<ApiSkillDto> = emptyList()
) {
    fun toDomainModel(): Roadmap {
        // Группируем навыки по родительским ID для создания тем
        val rootSkills = skills.filter { it.parentId == null }
        val themes = rootSkills.mapIndexed { index, rootSkill ->
            val themeSkills = skills.filter { skill -> 
                skill.parentId == rootSkill.nodeId || skill.nodeId == rootSkill.nodeId
            }
            
            ThemeDto(
                id = rootSkill.nodeId,
                title = rootSkill.skillName,
                description = "" /* API не предоставляет описание темы */,
                skills = themeSkills.map { it.toSkillDto() },
                position = index
            )
        }
        
        // Создаем общее количество узлов и завершенных узлов
        val totalNodes = skills.size
        val completedNodes = skills.count { it.status == "finished" }

        return Roadmap(
            id = 1, // API не предоставляет ID для роадмапа
            title = "Карта развития $specialization",
            description = "Карта развития для $grade $specialization",
            profession = specialization,
            themes = themes.map { it.toDomainModel() },
            totalNodes = totalNodes,
            completedNodes = completedNodes
        )
    }
}

// DTO, соответствующий структуре API
data class ApiSkillDto(
    @SerializedName("node_id") val nodeId: Int,
    @SerializedName("skill_id") val skillId: Int,
    @SerializedName("parent_id") val parentId: Int?,
    @SerializedName("skill_name") val skillName: String,
    @SerializedName("status") val status: String = "not_started"
) {
    fun toSkillDto(): SkillDto {
        return SkillDto(
            id = nodeId,
            name = skillName,
            importance = 50, // API не предоставляет важность, используем среднее значение
            description = "", // API не предоставляет описание навыка
            status = status,
            relatedSkills = emptyList() // API не предоставляет связанные навыки
        )
    }
}

data class ThemeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("skills") val skills: List<SkillDto> = emptyList(),
    @SerializedName("position") val position: Int = 0
) {
    fun toDomainModel(): RoadmapTheme {
        return RoadmapTheme(
            id = id,
            title = title,
            description = description,
            skills = skills.map { it.toDomainModel() },
            position = position
        )
    }
}

data class SkillDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("importance") val importance: Int,
    @SerializedName("description") val description: String,
    @SerializedName("status") val status: String = "not_started",
    @SerializedName("related_skills") val relatedSkills: List<Int> = emptyList()
) {
    fun toDomainModel(): RoadmapSkill {
        return RoadmapSkill(
            id = id,
            name = name,
            importance = importance,
            description = description,
            status = when(status.lowercase()) {
                "in_progress" -> NodeStatus.IN_PROGRESS
                "learned" -> NodeStatus.LEARNED
                else -> NodeStatus.NOT_STARTED
            },
            relatedSkills = relatedSkills
        )
    }
}

data class UpdateNodeStatusRequest(
    @SerializedName("node_id") val nodeId: Int,
    @SerializedName("status") val status: String
)

data class UpdateNodeStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
