package com.example.procareerv2.data.remote.api

import com.example.procareerv2.data.remote.dto.RoadmapResponse
import com.example.procareerv2.data.remote.dto.UpdateNodeStatusRequest
import com.example.procareerv2.data.remote.dto.UpdateNodeStatusResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RoadmapApi {
    @GET("recommendations/users/{userId}/roadmap")
    suspend fun getUserRoadmap(@Path("userId") userId: Int): RoadmapResponse
    
    @POST("recommendations/users/{userId}/roadmap/update-node-status")
    suspend fun updateNodeStatus(
        @Path("userId") userId: Int,
        @Body request: UpdateNodeStatusRequest
    ): UpdateNodeStatusResponse
}
