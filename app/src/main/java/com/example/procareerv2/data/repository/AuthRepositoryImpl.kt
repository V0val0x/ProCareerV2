package com.example.procareerv2.data.repository

import android.util.Log
import com.example.procareerv2.data.local.PreferencesManager
import com.example.procareerv2.data.local.UserPreferencesManager
import com.example.procareerv2.data.remote.api.AuthApi
import com.example.procareerv2.data.remote.dto.InterestDto
import com.example.procareerv2.data.remote.dto.LoginRequest
import com.example.procareerv2.data.remote.dto.RegisterRequest
import com.example.procareerv2.data.remote.dto.UserProfileRequest
import com.example.procareerv2.domain.model.Interest
import com.example.procareerv2.domain.model.User
import com.example.procareerv2.domain.repository.AuthRepository
import com.example.procareerv2.domain.repository.VacancyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val preferencesManager: PreferencesManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val externalScope: CoroutineScope,
    private val vacancyRepository: VacancyRepository
) : AuthRepository {

    /**
     * Приватная функция для парсинга вакансий по специализации пользователя
     * Запускает асинхронный процесс парсинга вакансий, не блокирует основной поток
     */
    private fun parseVacanciesForUser(user: User) {
        // Проверяем, что у пользователя есть специализация
        val specialization = user.specialization
        if (!specialization.isNullOrBlank()) {
            externalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    Log.d("AuthRepository", "Запускаем парсинг вакансий по специализации: $specialization")
                    vacancyRepository.parseHhVacancies(specialization, 100)
                        .onSuccess { vacancies ->
                            Log.d("AuthRepository", "Успешно получено ${vacancies.size} вакансий по специализации")
                        }
                        .onFailure { error ->
                            Log.e("AuthRepository", "Ошибка при парсинге вакансий: ${error.message}")
                        }
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Исключение при парсинге вакансий: ${e.message}")
                }
            }
        } else {
            Log.d("AuthRepository", "Парсинг вакансий не запущен: специализация пользователя не указана")
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            Log.d("AuthRepository", "Attempting to login with email: $email")
            val response = authApi.login(LoginRequest(email, password))
            Log.d("AuthRepository", "Login response received: success=${response.data.token.isNotBlank()}")
            
            if (response.data.token.isNotBlank()) {
                // Сервер возвращает id как user_id при логине
                val userId = response.data.user_id
                Log.d("AuthRepository", "Login successful, received user_id=$userId")
                
                val user = User(
                    // Используем user_id из ответа API
                    id = userId,
                    name = "", // Имя будет заполнено при получении профиля
                    email = email,
                    token = response.data.token
                )
                Log.d("AuthRepository", "Saving user: id=${user.id}, token=${user.token.take(10)}...")
                preferencesManager.saveUser(user)
                
                // Запускаем загрузку профиля в отдельном корутинном контексте, чтобы она не блокировала логин
                externalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        Log.d("AuthRepository", "Fetching user profile after successful login")
                        fetchUserProfile(user.id).onSuccess { updatedUser ->
                            // Запускаем парсинг вакансий по специализации после успешного получения профиля
                            parseVacanciesForUser(updatedUser)
                        }
                    } catch (e: Exception) {
                        // Игнорируем ошибки при получении профиля, т.к. основная задача - авторизация
                        // и она уже успешно выполнена
                        Log.e("AuthRepository", "Failed to fetch profile after login: ${e.message}", e)
                    }
                }
                
                Result.success(user)
            } else {
                Log.e("AuthRepository", "Login failed: ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            val response = authApi.register(RegisterRequest(name, email, password))
            val user = User(
                id = response.data.id, // Используем id вместо user_id, т.к. сервер возвращает правильный id
                name = name,
                email = email,
                token = response.data.token
            )
            preferencesManager.saveUser(user)
            
            // При регистрации не запускаем парсинг сразу, т.к. специализация ещё не указана
            // Парсинг будет запущен после обновления профиля с указанием специализации
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserFlow(): Flow<User?> {
        return preferencesManager.getUserFlow()
    }

    override suspend fun logout() {
        try {
            // Очищаем все данные пользователя
            preferencesManager.resetAllData()
            // Очищаем учетные данные
            userPreferencesManager.clearCredentials()
            Log.d("AuthRepository", "Logout: all user data and login credentials cleared")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error during logout: ${e.message}")
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        val user = preferencesManager.getUser()
        return user != null && user.token.isNotBlank()
    }

    override suspend fun updateUserInterests(interests: List<Interest>): Result<User> {
        return try {
            val currentUser = preferencesManager.getUser() ?: return Result.failure(Exception("User not found"))
            
            try {
                val interestNames = interests.map { it.name }
                val request = UserProfileRequest(
                    email = currentUser.email,
                    name = currentUser.name,
                    grade = currentUser.position ?: "",
                    specialization = currentUser.specialization ?: "",
                    interests = interestNames
                )
                val response = authApi.updateUserProfile(currentUser.id, request)
                
                // API now returns user object directly
                val updatedUser = response.toDomainUser(currentUser.token)
                preferencesManager.saveUser(updatedUser)
                return Result.success(updatedUser)
            } catch (e: Exception) {
                // В случае ошибки запроса к серверу, обновляем только локальные данные
                val updatedUser = currentUser.copy(interests = interests)
                preferencesManager.saveUser(updatedUser)
                Result.success(updatedUser)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(user: User): Result<User> {
        return try {
            // Проверяем наличие токена (значит пользователь авторизован)
            if (user.token.isBlank()) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            // Создаем объект запроса
            val interestNames = user.interests.map { it.name }
            val request = UserProfileRequest(
                email = user.email,
                name = user.name,
                grade = user.position ?: "", // Используем position как grade
                specialization = user.specialization ?: "", // Используем specialization также для specialization
                interests = interestNames
            )
            
            // Отправляем запрос на сервер
            try {
                val response = authApi.updateUserProfile(user.id, request)
                
                // API now returns user object directly
                val updatedUser = response.toDomainUser(user.token)
                
                // Сохраняем обновленные данные локально
                preferencesManager.saveUser(updatedUser)
                
                // Запускаем парсинг вакансий после обновления профиля, особенно если обновилась специализация
                parseVacanciesForUser(updatedUser)
                
                Result.success(updatedUser)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error updating profile: ${e.message}")
                e.printStackTrace()
                
                // В случае ошибки запроса к серверу, обновляем только локальные данные
                preferencesManager.saveUser(user)
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchUserProfile(userId: Int): Result<User> {
        return withContext(kotlinx.coroutines.Dispatchers.IO + NonCancellable) {
            try {
                val currentUser = preferencesManager.getUser()
                Log.d("AuthRepository", "Fetching profile for user $userId, current user: ${currentUser?.id}, token available: ${!currentUser?.token.isNullOrBlank()}")
                
                // Проверяем, что у пользователя есть токен для авторизации запросов
                if (currentUser?.token.isNullOrBlank()) {
                    Log.e("AuthRepository", "Cannot fetch profile: User not authenticated (no token)")
                    return@withContext Result.failure(Exception("User not authenticated"))
                }
                
                // Делаем запрос к API
                try {
                    Log.d("AuthRepository", "Making API call to get user profile for ID: $userId via AuthApi.getUserProfile()")
                    val response = authApi.getUserProfile(userId)
                    Log.d("AuthRepository", "Profile API response received: ${response}")
                    
                    // Преобразуем ответ в доменную модель
                    val updatedUser = response.toDomainUser(currentUser!!.token)
                    
                    // Проверка и исправление имени пользователя
                    val finalUser = if (updatedUser.name.isBlank()) {
                        // Если имя пустое, используем имя из текущего пользователя
                        Log.d("AuthRepository", "User name is blank, using current user name: ${currentUser.name}")
                        updatedUser.copy(name = currentUser.name)
                    } else {
                        updatedUser
                    }
                    
                    Log.d("AuthRepository", "Profile fetched successfully: id=${finalUser.id}, name=${finalUser.name}, email=${finalUser.email}")
                    preferencesManager.saveUser(finalUser)
                    return@withContext Result.success(finalUser)
                } catch (e: Exception) {
                    Log.e("AuthRepository", "API request failed: ${e.javaClass.simpleName}: ${e.message}")
                    // Dump the stack trace for debugging
                    e.printStackTrace()
                    // Если не удалось получить данные с сервера, используем локальные данные
                    if (currentUser != null) {
                        Log.d("AuthRepository", "Falling back to local user data: ${currentUser.name}")
                        return@withContext Result.success(currentUser)
                    } else {
                        throw e  // Перебрасываем исключение, если нет локальных данных
                    }
                }
            } catch (e: Exception) {
                // Если не удалось получить данные с сервера, используем локальные данные
                Log.e("AuthRepository", "Error fetching profile: ${e.message}")
                e.printStackTrace() // Print the full stack trace for debugging
                val localUser = preferencesManager.getUser()
                return@withContext if (localUser != null) {
                    Log.d("AuthRepository", "Using local user data as fallback: ${localUser.name}")
                    Result.success(localUser)
                } else {
                    Result.failure(e)
                }
            }
        }
    }
}