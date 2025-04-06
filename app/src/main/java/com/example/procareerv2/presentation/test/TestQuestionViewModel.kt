package com.example.procareerv2.presentation.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.procareerv2.data.local.PreferencesManager
import com.example.procareerv2.domain.model.Question
import com.example.procareerv2.domain.repository.TestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TestQuestionUiState(
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val currentQuestion: Question? = null,
    val selectedAnswerId: Int? = null,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val isLoading: Boolean = false,
    val isTestCompleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TestQuestionViewModel @Inject constructor(
    private val testRepository: TestRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestQuestionUiState(isLoading = true))
    val uiState: StateFlow<TestQuestionUiState> = _uiState.asStateFlow()

    fun startTest(testId: Int) {
        viewModelScope.launch {
            val userId = preferencesManager.getUser()?.id ?: 1 // Default to 1 if not logged in

            testRepository.startTest(testId, userId)
                .onSuccess { questions ->
                    _uiState.update { state ->
                        state.copy(
                            questions = questions,
                            totalQuestions = questions.size,
                            currentQuestionIndex = 0,
                            currentQuestion = if (questions.isNotEmpty()) questions[0] else null,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Ошибка загрузки вопросов"
                        )
                    }
                }
        }
    }

    fun selectAnswer(answerId: Int) {
        _uiState.update { it.copy(selectedAnswerId = answerId) }
    }

    fun nextQuestion() {
        val currentState = _uiState.value

        // Check if the selected answer is correct
        val isCorrect = currentState.currentQuestion?.answers?.find {
            it.id == currentState.selectedAnswerId
        }?.isRight ?: false

        // Update correct answers count if the answer is correct
        val newCorrectAnswers = if (isCorrect) {
            currentState.correctAnswers + 1
        } else {
            currentState.correctAnswers
        }

        // Check if this is the last question
        if (currentState.currentQuestionIndex == currentState.totalQuestions - 1) {
            // This was the last question, complete the test
            completeTest(newCorrectAnswers)
        } else {
            // Move to the next question
            val nextIndex = currentState.currentQuestionIndex + 1
            _uiState.update { state ->
                state.copy(
                    currentQuestionIndex = nextIndex,
                    currentQuestion = state.questions[nextIndex],
                    selectedAnswerId = null,
                    correctAnswers = newCorrectAnswers
                )
            }
        }
    }

    private fun completeTest(correctAnswers: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val testId = currentState.questions.firstOrNull()?.id ?: 0
            val userId = preferencesManager.getUser()?.id ?: 1

            testRepository.submitTestResults(testId, userId, correctAnswers)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isTestCompleted = true,
                            correctAnswers = correctAnswers
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            error = exception.message ?: "Ошибка отправки результатов"
                        )
                    }
                }
        }
    }
}