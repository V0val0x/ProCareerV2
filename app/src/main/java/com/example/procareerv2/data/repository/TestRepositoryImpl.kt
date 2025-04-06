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
                    duration = dto.duration,
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
            val response = testApi.startTest(StartTestRequest(testId, userId))
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
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitTestResults(testId: Int, userId: Int, correctAnswers: Int): Result<Unit> {
        return try {
            testApi.submitTestResults(
                TestResultRequest(
                    id_test = testId,
                    id_user = userId,
                    number_of_correct_answers = correctAnswers
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}