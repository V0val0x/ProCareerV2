package com.example.procareerv2.domain.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val token: String,
    val profileImage: String? = null,
    val position: String? = null,
    val interests: List<Interest> = emptyList()
)