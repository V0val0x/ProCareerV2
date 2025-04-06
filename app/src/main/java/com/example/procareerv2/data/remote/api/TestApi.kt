package com.example.procareerv2.data.remote.api

import com.example.procareerv2.data.remote.dto.StartTestRequest
import com.example.procareerv2.data.remote.dto.TestListResponse
import com.example.procareerv2.data.remote.dto.TestResponse
import com.example.procareerv2.data.remote.dto.TestResultRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TestApi {
    @GET("tests")
    suspend fun getTests(): TestListResponse

    @POST("tests/start")
    suspend fun startTest(@Body request: StartTestRequest): TestResponse

    @POST("tests/end")
    suspend fun submitTestResults(@Body request: TestResultRequest)
}