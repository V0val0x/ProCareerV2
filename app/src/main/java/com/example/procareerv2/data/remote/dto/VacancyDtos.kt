package com.example.procareerv2.data.remote.dto

data class VacancyListResponse(
    val data: List<VacancyDto>,
    val message: String,
    val errors: String
)

data class VacancyDto(
    val id: Int,
    val title: String,
    val grade: String,
    //val tags: List<String>,
    val description: String? = null,
    val responsibilities: List<String>? = null,
    val requirements: List<String>? = null,
    val technologies: List<String>? = null
)