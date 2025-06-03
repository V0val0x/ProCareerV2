package com.example.procareerv2.domain.repository

import com.example.procareerv2.domain.model.Vacancy

interface VacancyRepository {
    /**
     * Получение рекомендуемых вакансий с пагинацией
     * @param userId ID пользователя
     * @param page Номер страницы (начиная с 1)
     * @param pageSize Количество вакансий на странице
     */
    suspend fun getVacancies(userId: Int, page: Int = 1, pageSize: Int = 10): Result<List<Vacancy>>
    
    /**
     * Получение детальной информации о вакансии по её ID
     * @param vacancyId ID вакансии
     */
    suspend fun getVacancy(vacancyId: Int): Result<Vacancy>
    
    /**
     * Парсит вакансии с HH.ru по заданным ключевым словам
     * @param keywords Ключевые слова для поиска (обычно специализация пользователя)
     * @param perPage Количество вакансий для парсинга (по умолчанию 100)
     */
    suspend fun parseHhVacancies(keywords: String, perPage: Int = 100): Result<List<Vacancy>>
}