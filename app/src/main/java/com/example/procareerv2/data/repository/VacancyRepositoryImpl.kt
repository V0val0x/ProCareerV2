package com.example.procareerv2.data.repository

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
}