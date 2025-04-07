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

data class VacancyListUiState(
    val vacancies: List<Vacancy> = emptyList(),
    val filteredVacancies: List<Vacancy> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VacancyListViewModel @Inject constructor(
    private val vacancyRepository: VacancyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VacancyListUiState(isLoading = true))
    val uiState: StateFlow<VacancyListUiState> = _uiState.asStateFlow()

    fun loadVacancies() {
        viewModelScope.launch {
            vacancyRepository.getVacancies()
                .onSuccess { vacancies ->
                    _uiState.update {
                        it.copy(
                            vacancies = vacancies,
                            filteredVacancies = vacancies,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Ошибка загрузки вакансий"
                        )
                    }
                }
        }
    }

    fun searchVacancies(query: String) {
        val currentVacancies = _uiState.value.vacancies

        if (query.isBlank()) {
            _uiState.update { it.copy(filteredVacancies = currentVacancies) }
            return
        }

        val filtered = currentVacancies.filter { vacancy ->
            vacancy.title.contains(query, ignoreCase = true) ||
                    //vacancy.tags.any { it.contains(query, ignoreCase = true) } ||
                    vacancy.grade.contains(query, ignoreCase = true)
        }

        _uiState.update { it.copy(filteredVacancies = filtered) }
    }
}