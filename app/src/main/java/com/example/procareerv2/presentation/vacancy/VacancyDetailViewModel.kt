package com.example.procareerv2.presentation.vacancy

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
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VacancyDetailViewModel @Inject constructor(
    private val vacancyRepository: VacancyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VacancyDetailUiState(isLoading = true))
    val uiState: StateFlow<VacancyDetailUiState> = _uiState.asStateFlow()

    fun loadVacancy(vacancyId: Int) {
        viewModelScope.launch {
            // In a real app, you would fetch a specific vacancy by ID
            // For this example, we'll just get all vacancies and find the one with matching ID
            vacancyRepository.getVacancies()
                .onSuccess { vacancies ->
                    val vacancy = vacancies.find { it.id == vacancyId }
                    if (vacancy != null) {
                        _uiState.update { it.copy(vacancy = vacancy, isLoading = false, error = null) }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Вакансия не найдена"
                            )
                        }
                    }
                }
                .onFailure { exception ->
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