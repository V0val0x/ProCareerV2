package com.example.procareerv2.data.remote.api

import com.example.procareerv2.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("users/{userId}/profile")
    suspend fun getUserProfile(@Path("userId") userId: Int): UserProfileResponse

    @PUT("users/{userId}/profile")
    suspend fun updateUserProfile(
        @Path("userId") userId: Int,
        @Body user: UserProfileRequest
    ): UserProfileResponse
}