package com.example.procareerv2.presentation.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.domain.model.Test
import com.example.procareerv2.domain.repository.TestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TestListUiState(
    val tests: List<Test> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TestListViewModel @Inject constructor(
    private val testRepository: TestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestListUiState(isLoading = true))
    val uiState: StateFlow<TestListUiState> = _uiState.asStateFlow()

    fun loadTests() {
        viewModelScope.launch {
            testRepository.getTests()
                .onSuccess { tests ->
                    _uiState.update { it.copy(tests = tests, isLoading = false, error = null) }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Ошибка загрузки тестов"
                        )
                    }
                }
        }
    }
}