package com.example.procareerv2.presentation.vacancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.domain.model.Vacancy
import com.example.procareerv2.domain.repository.VacancyRepository
import com.example.procareerv2.data.local.PreferencesManager
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
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val isLastPage: Boolean = false,
    val searchQuery: String = ""
)

@HiltViewModel
class VacancyListViewModel @Inject constructor(
    private val vacancyRepository: VacancyRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VacancyListUiState(isLoading = true))
    val uiState: StateFlow<VacancyListUiState> = _uiState.asStateFlow()
    
    // Количество вакансий на странице
    private val pageSize: Int = 10
    
    // Загрузка данных происходит по требованию из UI

    fun loadVacancies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val user = preferencesManager.getUser()
            val userId = user?.id ?: -1
            loadVacanciesPage(1, userId)
        }
    }
    
    fun loadMoreVacancies() {
        val currentState = _uiState.value
        
        // Проверяем, не загружаем ли мы уже данные и не достигли ли последней страницы
        if (currentState.isLoading || currentState.isLoadingMore || currentState.isLastPage) {
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            
            val user = preferencesManager.getUser()
            val userId = user?.id ?: -1
            loadVacanciesPage(currentState.currentPage + 1, userId)
        }
    }
    
    private suspend fun loadVacanciesPage(page: Int, userId: Int) {
        vacancyRepository.getVacancies(userId, page, pageSize)
            .onSuccess { newVacancies ->
                _uiState.update { currentState ->
                    // Если страница первая, заменяем список, иначе добавляем к существующему
                    val allVacancies = if (page == 1) newVacancies else currentState.vacancies + newVacancies
                    val filteredVacancies = if (currentState.searchQuery.isBlank()) {
                        allVacancies
                    } else {
                        filterVacancies(allVacancies, currentState.searchQuery)
                    }
                    
                    currentState.copy(
                        vacancies = allVacancies,
                        filteredVacancies = filteredVacancies,
                        isLoading = false,
                        isLoadingMore = false,
                        error = null,
                        currentPage = page,
                        isLastPage = newVacancies.size < pageSize // Если получили меньше записей, чем размер страницы
                    )
                }
            }
            .onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = exception.message ?: "Ошибка загрузки вакансий"
                    )
                }
            }
    }

    fun searchVacancies(query: String) {
        val currentVacancies = _uiState.value.vacancies
        
        val filtered = filterVacancies(currentVacancies, query)
        
        _uiState.update { 
            it.copy(
                filteredVacancies = filtered,
                searchQuery = query
            ) 
        }
    }
    
    private fun filterVacancies(vacancies: List<Vacancy>, query: String): List<Vacancy> {
        if (query.isBlank()) {
            return vacancies
        }
        
        return vacancies.filter { vacancy ->
            vacancy.title.contains(query, ignoreCase = true) ||
                    vacancy.grade.contains(query, ignoreCase = true) ||
                    (vacancy.employer_name?.contains(query, ignoreCase = true) == true)
        }
    }
}