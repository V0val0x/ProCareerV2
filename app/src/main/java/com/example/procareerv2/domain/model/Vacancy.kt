package com.example.procareerv2.domain.model

data class Vacancy(
    val id: Int,
    val title: String,
    val grade: String,
    val url: String? = null,
    val employer_name: String? = null,
    val description: String? = null,
    // Эти поля могут быть не нужны с новым API, но оставляем их для обратной совместимости
    val responsibilities: List<String>? = null,
    val requirements: List<String>? = null,
    val technologies: List<String>? = null
)