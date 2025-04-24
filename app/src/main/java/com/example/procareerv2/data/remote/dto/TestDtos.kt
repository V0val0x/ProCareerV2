package com.example.procareerv2.data.remote.dto

data class TestListResponse(
    val data: List<TestDto>,
    val message: String,
    val errors: String
)

data class TestDto(
    val id: Int,
    val title: String,
    val description: String?,
    val average_passing_time: Int?,
    val id_skill: Int?
)

data class StartTestRequest(
    val id_test: Long,  // int64 в Go = Long в Kotlin
    val id_user: Long   // int64 в Go = Long в Kotlin
)

data class TestResponse(
    val data: TestDataDto,
    val message: String,
    val errors: String
)

data class TestDataDto(
    val number_of_questions: Int = 0,
    val questions: List<QuestionDto> = emptyList()
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
    val id_test: Long,           // int64 в Go = Long в Kotlin
    val id_user: Long,           // int64 в Go = Long в Kotlin
    val number_of_correct_answers: Byte  // int8 в Go = Byte в Kotlin
)