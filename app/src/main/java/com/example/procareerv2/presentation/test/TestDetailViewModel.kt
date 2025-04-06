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

data class TestDetailUiState(
    val test: Test? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TestDetailViewModel @Inject constructor(
    private val testRepository: TestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestDetailUiState(isLoading = true))
    val uiState: StateFlow<TestDetailUiState> = _uiState.asStateFlow()

    fun loadTest(testId: Int) {
        viewModelScope.launch {
            // In a real app, you would fetch a specific test by ID
            // For this example, we'll just get all tests and find the one with matching ID
            testRepository.getTests()
                .onSuccess { tests ->
                    val test = tests.find { it.id == testId }
                    if (test != null) {
                        _uiState.update { it.copy(test = test, isLoading = false, error = null) }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Тест не найден"
                            )
                        }
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Ошибка загрузки теста"
                        )
                    }
                }
        }
    }

    fun startTest() {
        // This would be called when the user clicks the start button
        // In a real app, you might want to do some preparation here
    }
}