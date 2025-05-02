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
                    duration = dto.duration ?: 0,
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
                id_test = testId.toLong(),
                id_user = userId.toLong()
            )
            println("[TestRepository] Created request: $request")
            
            println("[TestRepository] Sending POST request to /tests/start")
            val response = testApi.startTest(request)
            println("[TestRepository] Got response: $response")
            println("[TestRepository] Response data: ${response.data}")
            println("[TestRepository] Questions count: ${response.data.questions.size}")
            
            // Even if we get a 500 error, if we have valid data, let's use it
            if (response.data.questions.isNotEmpty()) {
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
                return Result.success(questions)
            }
            
            // If we have no questions but have errors, return the error
            if (response.errors.isNotEmpty()) {
                println("[TestRepository] Error in response: ${response.errors}")
                return Result.failure(Exception(response.errors))
            }
            
            // If we have neither questions nor errors, return empty list
            Result.success(emptyList())
        } catch (e: Exception) {
            println("Error starting test: ${e.message}")
            // If we get a 500 error but have valid data in the response, try to extract it
            if (e is retrofit2.HttpException && e.code() == 500) {
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    println("[TestRepository] Error body: $errorBody")
                    
                    if (errorBody != null) {
                        println("[TestRepository] Found error response, attempting to parse")
                        // Parse the error body using Gson
                        val gson = com.google.gson.Gson()
                        val type = object : com.google.gson.reflect.TypeToken<com.example.procareerv2.data.remote.dto.TestResponse>() {}.type
                        val response = gson.fromJson<com.example.procareerv2.data.remote.dto.TestResponse>(errorBody, type)
                        
                        // Check if we have questions in the response
                        if (response.data.questions.isNotEmpty()) {
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
                            println("[TestRepository] Successfully parsed ${questions.size} questions from error response")
                            return Result.success(questions)
                        } else {
                            println("[TestRepository] No questions found in response")
                            // If we have an error message, return it
                            if (response.errors.isNotEmpty()) {
                                return Result.failure(Exception(response.errors))
                            }
                        }
                    }
                } catch (parseError: Exception) {
                    println("[TestRepository] Failed to parse error body: ${parseError.message}")
                    println("[TestRepository] Parse error stack trace: ${parseError.stackTraceToString()}")
                }
            }
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