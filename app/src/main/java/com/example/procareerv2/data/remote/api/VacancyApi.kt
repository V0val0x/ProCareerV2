package com.example.procareerv2.data.remote.api

import com.example.procareerv2.data.remote.dto.HhParserResponse
import com.example.procareerv2.data.remote.dto.VacancyDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VacancyApi {
    @GET("recommendations/get/{userId}")
    suspend fun getVacancies(
        @Path("userId") userId: Int,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): List<VacancyDto>
    
    @GET("recommendations/vacancies/{vacancy_id}")
    suspend fun getVacancy(
        @Path("vacancy_id") vacancyId: Int
    ): VacancyDto
    
    @POST("recommendations/parse-hh-vacancies")
    suspend fun parseHhVacancies(
        @Query("text") text: String,
        @Query("per_page") perPage: Int = 100
    ): HhParserResponse
}