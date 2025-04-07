package com.example.procareerv2.domain.model

data class Question(
    val id: Int,
    val question: String,
    val answers: List<Answer>? = null
)