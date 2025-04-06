package com.example.procareerv2.domain.repository

import com.example.procareerv2.domain.model.Test
import com.example.procareerv2.domain.model.Question

interface TestRepository {
    suspend fun getTests(): Result<List<Test>>
    suspend fun startTest(testId: Int, userId: Int): Result<List<Question>>
    suspend fun submitTestResults(testId: Int, userId: Int, correctAnswers: Int): Result<Unit>
}