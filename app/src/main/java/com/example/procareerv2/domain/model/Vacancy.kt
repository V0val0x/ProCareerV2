package com.example.procareerv2.domain.model

data class Vacancy(
    val id: Int,
    val title: String,
    val grade: String,
    //val tags: List<String>? = null,
    val description: String? = null,
    val responsibilities: List<String>? = null,
    val requirements: List<String>? = null,
    val technologies: List<String>? = null
)