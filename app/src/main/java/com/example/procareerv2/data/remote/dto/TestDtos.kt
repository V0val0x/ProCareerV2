package com.example.procareerv2.data.remote.dto

data class TestListResponse(
    val data: List<TestDto>,
    val message: String,
    val errors: String
)

data class TestDto(
    val id: Int,
    val title: String,
    val duration: Int
)

data class StartTestRequest(
    val id_test: Int,
    val id_user: Int
)

data class TestResponse(
    val data: TestDataDto,
    val message: String,
    val errors: String
)

data class TestDataDto(
    val number_of_questions: Int,
    val questions: List<QuestionDto>
)

data class QuestionDto(
    val id: Int,
    val question: String,
    val answers: List<AnswerDto>
)

data class AnswerDto(
    val id: Int,
    val answer: String,
    val is_right: Boolean
)

data class TestResultRequest(
    val id_test: Int,
    val id_user: Int,
    val number_of_correct_answers: Int
)