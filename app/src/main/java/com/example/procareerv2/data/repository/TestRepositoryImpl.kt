package com.example.procareerv2.data.repository

import com.example.procareerv2.data.remote.api.TestApi
import com.example.procareerv2.data.remote.dto.StartTestRequest
import com.example.procareerv2.data.remote.dto.TestResultRequest
import com.example.procareerv2.domain.model.Answer
import com.example.procareerv2.domain.model.Question
import com.example.procareerv2.domain.model.Test
import com.example.procareerv2.domain.repository.TestRepository
import javax.inject.Inject

class TestRepositoryImpl @Inject constructor(
    private val testApi: TestApi
) : TestRepository {

    override suspend fun getTests(): Result<List<Test>> {
        return try {
            val response = testApi.getTests()
            val tests = response.data.map { dto ->
                Test(
                    id = dto.id,
                    title = dto.title,
                    duration = dto.average_passing_time ?: 0,
                    numberOfQuestions = 0 // Not provided in the list response
                )
            }
            Result.success(tests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startTest(testId: Int, userId: Int): Result<List<Question>> {
        return try {
            println("[TestRepository] Starting test with testId=$testId, userId=$userId")
            val request = StartTestRequest(
                id_test = testId.toLong(),  // Convert Int to Long
                id_user = userId.toLong()   // Convert Int to Long
            )
            println("[TestRepository] Created request: $request")
            
            println("[TestRepository] Sending POST request to /tests/start")
            val response = testApi.startTest(request)
            println("[TestRepository] Got response: $response")
            println("[TestRepository] Response data: ${response.data}")
            println("[TestRepository] Questions count: ${response.data.questions.size}")
            
            // Check for API errors
            if (response.errors.isNotEmpty()) {
                println("[TestRepository] Error in response: ${response.errors}")
                return Result.failure(Exception(response.errors))
            }
            
            // Map questions from response
            println("[TestRepository] Mapping questions from response")
            val questions = response.data.questions.map { questionDto ->
                Question(
                    id = questionDto.id,
                    question = questionDto.question,
                    answers = questionDto.answers.map { answerDto ->
                        Answer(
                            id = answerDto.id,
                            answer = answerDto.answer,
                            isRight = answerDto.is_right
                        )
                    }
                )
            }
            println("Mapped ${questions.size} questions")
            Result.success(questions)
        } catch (e: Exception) {
            println("Error starting test: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun submitTestResults(testId: Int, userId: Int, correctAnswers: Int): Result<Unit> {
        // Преобразуем типы для соответствия с бэкендом
        return try {
            println("Submitting test results: testId=$testId, userId=$userId, correctAnswers=$correctAnswers")
            val request = TestResultRequest(
                id_test = testId.toLong(),  // Int -> Long (int64)
                id_user = userId.toLong(),   // Int -> Long (int64)
                number_of_correct_answers = correctAnswers.toByte() // Int -> Byte (int8)
            )
            println("Sending result request: $request")
            
            testApi.submitTestResults(request)
            println("Test results submitted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error submitting test results: ${e.message}")
            Result.failure(e)
        }
    }
}