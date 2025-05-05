package com.example.procareerv2.domain.repository

import com.example.procareerv2.domain.model.Interest
import com.example.procareerv2.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>
    fun getUserFlow(): Flow<User?>
    suspend fun logout()
    suspend fun isLoggedIn(): Boolean
    suspend fun fetchUserProfile(userId: Int): Result<User>
    suspend fun updateUserInterests(interests: List<Interest>): Result<User>
    suspend fun updateUserProfile(user: User): Result<User>
}