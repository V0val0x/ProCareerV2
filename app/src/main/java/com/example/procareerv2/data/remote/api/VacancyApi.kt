package com.example.procareerv2.data.remote.api

import com.example.procareerv2.data.remote.dto.HhParserResponse
import com.example.procareerv2.data.remote.dto.VacancyDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VacancyApi {
    @GET("recommendations/vacancies/")
    suspend fun getVacancies(): List<VacancyDto>
    
    @POST("recommendations/parse-hh-vacancies")
    suspend fun parseHhVacancies(
        @Query("text") text: String,
        @Query("per_page") perPage: Int = 100
    ): HhParserResponse
}