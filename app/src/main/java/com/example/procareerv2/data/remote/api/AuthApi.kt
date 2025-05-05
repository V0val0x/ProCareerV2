package com.example.procareerv2.data.remote.api

import com.example.procareerv2.data.remote.dto.LoginRequest
import com.example.procareerv2.data.remote.dto.LoginResponse
import com.example.procareerv2.data.remote.dto.RegisterRequest
import com.example.procareerv2.data.remote.dto.RegisterResponse
import com.example.procareerv2.data.remote.dto.UserProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("recommendations/users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: Int): UserProfileResponse
}