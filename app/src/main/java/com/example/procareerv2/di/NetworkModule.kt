package com.example.procareerv2.di

import com.example.procareerv2.data.local.PreferencesManager
import com.example.procareerv2.data.remote.api.AuthApi
import com.example.procareerv2.data.remote.api.TestApi
import com.example.procareerv2.data.remote.api.VacancyApi
import com.example.procareerv2.data.remote.interceptor.AuthInterceptor
import com.example.procareerv2.data.repository.AuthRepositoryImpl
import com.example.procareerv2.data.repository.TestRepositoryImpl
import com.example.procareerv2.data.repository.VacancyRepositoryImpl
import com.example.procareerv2.domain.repository.AuthRepository
import com.example.procareerv2.domain.repository.TestRepository
import com.example.procareerv2.domain.repository.VacancyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    fun provideAuthApi(@AuthRetrofit retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
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
    fun provideAuthRepository(authApi: AuthApi, preferencesManager: PreferencesManager): AuthRepository {
        return AuthRepositoryImpl(authApi, preferencesManager)
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
}