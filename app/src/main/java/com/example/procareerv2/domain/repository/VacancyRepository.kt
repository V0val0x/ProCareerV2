package com.example.procareerv2.domain.repository

import com.example.procareerv2.domain.model.Vacancy

interface VacancyRepository {
    suspend fun getVacancies(): Result<List<Vacancy>>
}