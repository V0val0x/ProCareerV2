package com.example.procareerv2.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val data: LoginResponseData,
    val message: String,
    val errors: String
)

data class LoginResponseData(
    val token: String,
    val user_id: Int
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val data: RegisterResponseData,
    val message: String,
    val errors: String
)

data class RegisterResponseData(
    val id: Int,
    val token: String,
    val user_id: Int
)