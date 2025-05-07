package com.example.procareerv2.data.remote.api

import com.example.procareerv2.data.remote.dto.VacancyDto
import retrofit2.http.GET

interface VacancyApi {
    @GET("recommendations/vacancies/")
    suspend fun getVacancies(): List<VacancyDto>
}