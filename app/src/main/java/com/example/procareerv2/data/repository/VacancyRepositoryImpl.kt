package com.example.procareerv2.data.repository

import android.util.Log
import com.example.procareerv2.data.remote.api.VacancyApi
import com.example.procareerv2.domain.model.Vacancy
import com.example.procareerv2.domain.repository.VacancyRepository
import javax.inject.Inject

class VacancyRepositoryImpl @Inject constructor(
    private val vacancyApi: VacancyApi
) : VacancyRepository {

    override suspend fun getVacancies(): Result<List<Vacancy>> {
        return try {
            val response = vacancyApi.getVacancies()
            val vacancies = response.map { dto ->
                Vacancy(
                    id = dto.id,
                    title = dto.title,
                    grade = dto.grade,
                    url = dto.url,
                    employer_name = dto.employer_name,
                    description = dto.description,
                    responsibilities = dto.responsibilities,
                    requirements = dto.requirements,
                    technologies = dto.technologies
                )
            }
            Result.success(vacancies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun parseHhVacancies(keywords: String, perPage: Int): Result<List<Vacancy>> {
        return try {
            Log.d("VacancyRepository", "Парсинг вакансий с HH по ключевым словам: $keywords, количество: $perPage")
            val response = vacancyApi.parseHhVacancies(text = keywords, perPage = perPage)
            Log.d("VacancyRepository", "Получен ответ от парсера HH: ${response.status}")
            
            // В текущей версии API не возвращает вакансии напрямую, а просто запускает парсинг в фоне
            // Возвращаем пустой список, т.к. вакансии будут доступны позже через getVacancies()
            Result.success(emptyList())
        } catch (e: Exception) {
            Log.e("VacancyRepository", "Ошибка при парсинге вакансий: ${e.message}")
            Result.failure(e)
        }
    }
}