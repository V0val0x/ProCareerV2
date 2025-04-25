package com.example.procareerv2.data.remote.api

import com.example.procareerv2.data.remote.dto.VacancyListResponse
import retrofit2.http.GET

interface VacancyApi {
    @GET("api/vacancies")
    suspend fun getVacancies(): VacancyListResponse
}