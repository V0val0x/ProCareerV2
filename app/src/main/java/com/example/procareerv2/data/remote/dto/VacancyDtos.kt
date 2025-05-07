package com.example.procareerv2.data.remote.dto

// Данный класс не нужен для нового API, так как оно возвращает список напрямую, а не обертку
// Оставляем для обратной совместимости, если понадобится
data class VacancyListResponse(
    val data: List<VacancyDto>,
    val message: String = "",
    val errors: String = ""
)

data class VacancyDto(
    val id: Int,
    val title: String,
    val grade: String,
    val url: String? = null,
    val employer_name: String? = null,
    val description: String? = null,
    // Оставляем для обратной совместимости, но с новым API эти поля могут не использоваться
    val responsibilities: List<String>? = null,
    val requirements: List<String>? = null,
    val technologies: List<String>? = null
)