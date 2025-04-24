package com.example.procareerv2.data.repository

import com.example.procareerv2.data.local.PreferencesManager
import com.example.procareerv2.data.remote.api.AuthApi
import com.example.procareerv2.data.remote.dto.LoginRequest
import com.example.procareerv2.data.remote.dto.RegisterRequest
import com.example.procareerv2.domain.model.Interest
import com.example.procareerv2.domain.model.Skill
import com.example.procareerv2.domain.model.User
import com.example.procareerv2.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            val user = User(
                id = response.id,
                name = "", // Server doesn't return name in login response
                email = email,
                token = response.token
            )
            preferencesManager.saveUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            val response = authApi.register(RegisterRequest(name, email, password))
            val user = User(
                id = response.id,
                name = name,
                email = email,
                token = response.token
            )
            preferencesManager.saveUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserFlow(): Flow<User?> {
        return preferencesManager.getUserFlow()
    }

    override suspend fun logout() {
        preferencesManager.clearUser()
    }

    override suspend fun isLoggedIn(): Boolean {
        return preferencesManager.getUser() != null
    }

    override suspend fun updateUserSkills(skills: List<Skill>): Result<User> {
        return try {
            val currentUser = preferencesManager.getUser() ?: throw Exception("User not found")
            val updatedUser = currentUser.copy(skills = skills)
            preferencesManager.saveUser(updatedUser)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserInterests(interests: List<Interest>): Result<User> {
        return try {
            val currentUser = preferencesManager.getUser() ?: throw Exception("User not found")
            val updatedUser = currentUser.copy(interests = interests)
            preferencesManager.saveUser(updatedUser)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(user: User): Result<User> {
        return try {
            preferencesManager.saveUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}