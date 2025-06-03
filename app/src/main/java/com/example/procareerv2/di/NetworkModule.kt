package com.example.procareerv2.di

import com.example.procareerv2.data.local.PreferencesManager
import com.example.procareerv2.data.local.UserPreferencesManager
import com.example.procareerv2.data.remote.api.AuthApi
import com.example.procareerv2.data.remote.api.RoadmapApi
import com.example.procareerv2.data.remote.api.TestApi
import com.example.procareerv2.data.remote.api.VacancyApi
import com.example.procareerv2.data.remote.dto.LoginRequest
import com.example.procareerv2.data.remote.dto.LoginResponse
import com.example.procareerv2.data.remote.dto.RegisterRequest
import com.example.procareerv2.data.remote.dto.RegisterResponse
import com.example.procareerv2.data.remote.dto.UserProfileRequest
import com.example.procareerv2.data.remote.dto.UserProfileResponse
import com.example.procareerv2.data.remote.interceptor.AuthInterceptor
import com.example.procareerv2.data.repository.AuthRepositoryImpl
import com.example.procareerv2.data.repository.RoadmapRepositoryImpl
import com.example.procareerv2.data.repository.TestRepositoryImpl
import com.example.procareerv2.data.repository.VacancyRepositoryImpl
import com.example.procareerv2.domain.repository.AuthRepository
import com.example.procareerv2.domain.repository.RoadmapRepository
import com.example.procareerv2.domain.repository.TestRepository
import com.example.procareerv2.domain.repository.VacancyRepository
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2/"

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @AuthHttpClient
    @Provides
    @Singleton
    fun provideAuthOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @MainHttpClient
    @Provides
    @Singleton
    fun provideMainOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @AuthRetrofit
    @Provides
    @Singleton
    fun provideAuthRetrofit(@AuthHttpClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @MainRetrofit
    @Provides
    @Singleton
    fun provideMainRetrofit(@MainHttpClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    @Provides
    @Singleton
    fun provideAuthApi(
        @AuthRetrofit authRetrofit: Retrofit,
        @MainRetrofit mainRetrofit: Retrofit
    ): AuthApi {
        // u0418u0441u043fu043eu043bu044cu0437u0443u0435u043c @AuthRetrofit u0442u043eu043bu044cu043au043e u0434u043bu044f auth/login u0438 auth/register
        // u0414u043bu044f u043eu043fu0435u0440u0430u0446u0438u0439 u0441 u043fu0440u043eu0444u0438u043bu0435u043c (u043au043eu0442u043eu0440u044bu0435 u0442u0440u0435u0431u0443u044eu0442 u0430u0432u0442u043eu0440u0438u0437u0430u0446u0438u0438) u0438u0441u043fu043eu043bu044cu0437u0443u0435u043c MainRetrofit
        return object : AuthApi {
            private val authApiService = authRetrofit.create(AuthApi::class.java)
            private val mainApiService = mainRetrofit.create(AuthApi::class.java)

            override suspend fun login(request: LoginRequest): LoginResponse {
                return authApiService.login(request)
            }

            override suspend fun register(request: RegisterRequest): RegisterResponse {
                return authApiService.register(request)
            }

            override suspend fun getUserProfile(userId: Int): UserProfileResponse {
                return mainApiService.getUserProfile(userId)
            }

            override suspend fun updateUserProfile(
                userId: Int, 
                user: UserProfileRequest
            ): UserProfileResponse {
                return mainApiService.updateUserProfile(userId, user)
            }
        }
    }

    @Provides
    @Singleton
    fun provideTestApi(@MainRetrofit retrofit: Retrofit): TestApi {
        return retrofit.create(TestApi::class.java)
    }

    @Provides
    @Singleton
    fun provideVacancyApi(@MainRetrofit retrofit: Retrofit): VacancyApi {
        return retrofit.create(VacancyApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideRoadmapApi(@MainRetrofit retrofit: Retrofit): RoadmapApi {
        return retrofit.create(RoadmapApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi, 
        preferencesManager: PreferencesManager, 
        userPreferencesManager: UserPreferencesManager,
        externalScope: CoroutineScope,
        vacancyRepository: VacancyRepository
    ): AuthRepository {
        return AuthRepositoryImpl(authApi, preferencesManager, userPreferencesManager, externalScope, vacancyRepository)
    }

    @Provides
    @Singleton
    fun provideTestRepository(testApi: TestApi): TestRepository {
        return TestRepositoryImpl(testApi)
    }

    @Provides
    @Singleton
    fun provideVacancyRepository(vacancyApi: VacancyApi): VacancyRepository {
        return VacancyRepositoryImpl(vacancyApi)
    }
    
    @Provides
    @Singleton
    fun provideRoadmapRepository(roadmapApi: RoadmapApi): RoadmapRepository {
        return RoadmapRepositoryImpl(roadmapApi)
    }
}