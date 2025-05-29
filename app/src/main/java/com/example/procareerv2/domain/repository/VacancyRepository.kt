package com.example.procareerv2.domain.repository

import com.example.procareerv2.domain.model.Vacancy

interface VacancyRepository {
    suspend fun getVacancies(): Result<List<Vacancy>>
    
    /**
     * Парсит вакансии с HH.ru по заданным ключевым словам
     * @param keywords Ключевые слова для поиска (обычно специализация пользователя)
     * @param perPage Количество вакансий для парсинга (по умолчанию 100)
     */
    suspend fun parseHhVacancies(keywords: String, perPage: Int = 100): Result<List<Vacancy>>
}