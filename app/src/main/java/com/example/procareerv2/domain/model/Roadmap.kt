package com.example.procareerv2.domain.model

data class Roadmap(
    val id: Int,
    val title: String,
    val description: String,
    val profession: String,
    val skills: List<RoadmapSkill>
)

data class RoadmapSkill(
    val id: Int,
    val name: String,
    val importance: Int, // 1-100
    val description: String,
    val relatedSkills: List<Int> // IDs of related skills
)
