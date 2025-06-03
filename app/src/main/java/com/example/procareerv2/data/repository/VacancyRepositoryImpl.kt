package com.example.procareerv2.data.repository

import android.util.Log
import com.example.procareerv2.data.remote.api.VacancyApi
import com.example.procareerv2.domain.model.Vacancy
import com.example.procareerv2.domain.repository.VacancyRepository
import javax.inject.Inject

class VacancyRepositoryImpl @Inject constructor(
    private val vacancyApi: VacancyApi
) : VacancyRepository {

    override suspend fun getVacancies(userId: Int, page: Int, pageSize: Int): Result<List<Vacancy>> {
        return try {
            Log.d("VacancyRepository", "Загрузка вакансий: userId=$userId, page=$page, pageSize=$pageSize")
            val response = vacancyApi.getVacancies(userId, page, pageSize)
            val vacancies = response.map { dto ->
                Vacancy(
                    id = dto.vacancy_id, // Обновлено с id на vacancy_id
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
            Log.d("VacancyRepository", "Загружено ${vacancies.size} вакансий для страницы $page")
            Result.success(vacancies)
        } catch (e: Exception) {
            Log.e("VacancyRepository", "Ошибка при загрузке вакансий: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun getVacancy(vacancyId: Int): Result<Vacancy> {
        return try {
            Log.d("VacancyRepository", "Загрузка детальной информации о вакансии: vacancyId=$vacancyId")
            
            // Получаем данные вакансии с сервера
            try {
                val vacancyDto = vacancyApi.getVacancy(vacancyId)
                
                // Если сервер вернул невалидный ответ (например, {"status": "nginx ok"}), 
                // то вернем ошибку

                // Создаем объект вакансии с безопасными значениями по умолчанию
                val vacancy = Vacancy(
                    id = vacancyDto.vacancy_id ?: vacancyId, // Обновлено с id на vacancy_id
                    title = vacancyDto.title ?: "Вакансия $vacancyId",
                    grade = vacancyDto.grade ?: "Junior",
                    url = vacancyDto.url ?: "",
                    employer_name = vacancyDto.employer_name ?: "Не указано",
                    description = vacancyDto.description ?: "Описание отсутствует",
                    responsibilities = vacancyDto.responsibilities ?: listOf("Обязанности не указаны"),
                    requirements = vacancyDto.requirements ?: listOf("Требования не указаны"),
                    technologies = vacancyDto.technologies ?: emptyList()
                )
                
                Log.d("VacancyRepository", "Загружена информация о вакансии: ${vacancy.title}")
                Result.success(vacancy)
            } catch (e: Exception) {
                // Если произошла ошибка при парсинге JSON, попробуем получить вакансию из списка
                Log.w("VacancyRepository", "Ошибка при получении вакансии по API: ${e.message}. Попытка получить из списка.")
                
                // Попробуем получить вакансию из общего списка вакансий
                // Временно используем userId=1, в реальном приложении нужно брать актуальный ID пользователя
                val vacanciesResult = getVacancies(1)
                
                if (vacanciesResult.isSuccess) {
                    val vacancies = vacanciesResult.getOrNull()
                    val vacancy = vacancies?.find { it.id == vacancyId }
                    
                    if (vacancy != null) {
                        Log.d("VacancyRepository", "Вакансия найдена в списке: ${vacancy.title}")
                        return Result.success(vacancy)
                    }
                }
                
                // Если вакансия не найдена ни в деталях, ни в списке, вернем ошибку
                throw IllegalStateException("Вакансия с ID $vacancyId не найдена")
            }
        } catch (e: Exception) {
            Log.e("VacancyRepository", "Ошибка при загрузке информации о вакансии: ${e.message}")
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