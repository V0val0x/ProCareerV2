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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TestQuestionUiState(
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = -1, // Start at -1 to indicate no question loaded
    val currentQuestion: Question? = null,
    val selectedAnswerId: Int? = null,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val isLoading: Boolean = true, // Start with loading state
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

    private val userAnswers = mutableMapOf<Int, Int>() // questionId to answerId

    fun startTest(testId: Int) {
        println("[TestQuestionViewModel] startTest called with testId=$testId")
        viewModelScope.launch {
            try {
                println("[TestQuestionViewModel] Updating state to loading")
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                val user = preferencesManager.getUserFlow().first()
                val userId = user?.id ?: 1 // Default to 1 if not logged in
                println("[TestQuestionViewModel] Starting test $testId for user $userId")

                println("[TestQuestionViewModel] Calling repository.startTest")
                val result = testRepository.startTest(testId, userId)
                println("[TestQuestionViewModel] Got result from repository: $result")
                
                result.fold(
                    onSuccess = { questions ->
                        println("[TestQuestionViewModel] Success! Received ${questions.size} questions")
                        if (questions.isEmpty()) {
                            println("[TestQuestionViewModel] No questions received")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Нет доступных вопросов для этого теста"
                                )
                            }
                        } else {
                            println("[TestQuestionViewModel] Updating state with questions")
                            _uiState.update {
                                it.copy(
                                    questions = questions,
                                    currentQuestionIndex = 0,
                                    currentQuestion = questions[0],
                                    totalQuestions = questions.size,
                                    isLoading = false,
                                    error = null,
                                    selectedAnswerId = null // Reset selected answer
                                )
                            }
                        }
                    },
                    onFailure = { exception ->
                        println("[TestQuestionViewModel] Error starting test: ${exception.message}")
                        println("[TestQuestionViewModel] Stack trace: ${exception.stackTraceToString()}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Ошибка загрузки теста"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                println("[TestQuestionViewModel] Unexpected error: ${e.message}")
                println("[TestQuestionViewModel] Stack trace: ${e.stackTraceToString()}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Неожиданная ошибка"
                    )
                }
            }
        }
    }

    fun selectAnswer(answerId: Int) {
        _uiState.update { currentState ->
            currentState.currentQuestion?.let { question ->
                userAnswers[question.id] = answerId
            }
            currentState.copy(
                selectedAnswerId = answerId
            )
        }
    }

    fun nextQuestion() {
        _uiState.update { currentState ->
            val nextIndex = currentState.currentQuestionIndex + 1
            if (nextIndex < currentState.questions.size) {
                currentState.copy(
                    currentQuestionIndex = nextIndex,
                    currentQuestion = currentState.questions[nextIndex],
                    selectedAnswerId = null // Reset selected answer
                )
            } else {
                // Calculate results
                var correctCount = 0
                currentState.questions.forEach { question ->
                    val userAnswer = userAnswers[question.id]
                    val correctAnswer = question.answers?.find { it.isRight }?.id
                    if (userAnswer != null && correctAnswer != null && userAnswer == correctAnswer) {
                        correctCount++
                    }
                }
                
                // Submit results
                submitTestResults(correctCount, currentState.questions.size)
                
                currentState.copy(
                    isTestCompleted = true,
                    correctAnswers = correctCount
                )
            }
        }
    }

    private fun submitTestResults(correctAnswers: Int, totalQuestions: Int) {
        viewModelScope.launch {
            try {
                val user = preferencesManager.getUserFlow().first()
                val userId = user?.id ?: 1
                
                testRepository.submitTestResults(
                    userId = userId,
                    testId = uiState.value.questions.firstOrNull()?.id ?: 0,
                    correctAnswers = correctAnswers
                )
            } catch (e: Exception) {
                println("[TestQuestionViewModel] Error submitting results: ${e.message}")
            }
        }
    }

    private fun completeTest(correctAnswers: Int) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val testId = currentState.questions.firstOrNull()?.id ?: 0
                val userId = preferencesManager.getUser()?.id ?: 1
                
                println("Completing test: testId=$testId, userId=$userId, correctAnswers=$correctAnswers")
                
                testRepository.submitTestResults(testId, userId, correctAnswers)
                    .onSuccess {
                        println("Test completed successfully")
                        _uiState.update {
                            it.copy(
                                isTestCompleted = true,
                                correctAnswers = correctAnswers,
                                error = null
                            )
                        }
                    }
                    .onFailure { exception ->
                        println("Error completing test: ${exception.message}")
                        _uiState.update {
                            it.copy(
                                error = "Ошибка отправки результатов: ${exception.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                println("Unexpected error completing test: ${e.message}")
                _uiState.update {
                    it.copy(
                        error = "Неожиданная ошибка: ${e.message}"
                    )
                }
            }
        }
    }
}