package com.example.procareerv2.presentation.vacancy

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.domain.model.Vacancy
import com.example.procareerv2.domain.repository.VacancyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VacancyDetailUiState(
    val vacancy: Vacancy? = null,
    val vacancyId: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VacancyDetailViewModel @Inject constructor(
    private val vacancyRepository: VacancyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VacancyDetailUiState(isLoading = true))
    val uiState: StateFlow<VacancyDetailUiState> = _uiState.asStateFlow()
    
    // Хранение последнего загруженного ID вакансии для предотвращения повторных запросов
    private var lastLoadedVacancyId: Int = -1

    fun loadVacancy(vacancyId: Int, userId: Int) {
        // Проверяем, что ID вакансии правильный и не равен 0
        if (vacancyId <= 0) {
            Log.e("VacancyDetailViewModel", "Попытка загрузить вакансию с некорректным ID: $vacancyId")
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    error = "Некорректный ID вакансии. Пожалуйста, выберите вакансию из списка."
                )
            }
            return
        }
        
        // Если это тот же ID, что уже загружен и данные уже есть, пропускаем запрос
        if (vacancyId == lastLoadedVacancyId && uiState.value.vacancy != null) {
            Log.d("VacancyDetailViewModel", "Вакансия с ID $vacancyId уже загружена, пропускаем повторный запрос")
            return
        }
        
        Log.d("VacancyDetailViewModel", "Загрузка вакансии с ID: $vacancyId")
        lastLoadedVacancyId = vacancyId
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, vacancyId = vacancyId) }
            
            // Загружаем детали вакансии по ID через API метод
            vacancyRepository.getVacancy(vacancyId)
                .onSuccess { vacancy ->
                    Log.d("VacancyDetailViewModel", "Успешно загружена вакансия: ${vacancy.title}")
                    _uiState.update { it.copy(vacancy = vacancy, isLoading = false, error = null) }
                }
                .onFailure { exception ->
                    Log.e("VacancyDetailViewModel", "Ошибка загрузки вакансии с ID $vacancyId: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Ошибка загрузки вакансии"
                        )
                    }
                }
        }
    }
}