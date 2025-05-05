package com.example.procareerv2.data.remote.dto

data class UserProfileRequest(
    val email: String,
    val name: String,
    val grade: String,
    val specialization: String,
    val interests: List<String>
)
