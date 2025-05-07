package com.example.procareerv2.data.repository

import android.util.Log
import com.example.procareerv2.data.remote.api.RoadmapApi
import com.example.procareerv2.data.remote.dto.UpdateNodeStatusRequest
import com.example.procareerv2.domain.model.NodeStatus
import com.example.procareerv2.domain.model.Roadmap
import com.example.procareerv2.domain.repository.RoadmapRepository
import javax.inject.Inject

class RoadmapRepositoryImpl @Inject constructor(
    private val roadmapApi: RoadmapApi
) : RoadmapRepository {

    override suspend fun getUserRoadmap(userId: Int): Result<Roadmap> {
        return try {
            val response = roadmapApi.getUserRoadmap(userId)
            Result.success(response.toDomainModel())
        } catch (e: Exception) {
            Log.e("RoadmapRepository", "Error fetching roadmap: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateNodeStatus(userId: Int, nodeId: Int, status: NodeStatus): Result<Boolean> {
        return try {
            val statusString = when(status) {
                NodeStatus.NOT_STARTED -> "not_started"
                NodeStatus.IN_PROGRESS -> "in_progress"
                NodeStatus.LEARNED -> "learned"
            }
            
            val request = UpdateNodeStatusRequest(nodeId, statusString)
            val response = roadmapApi.updateNodeStatus(userId, request)
            
            // The API returns a message on success, so we'll check if the message contains an error indication
            // If no clear error is present, we assume success
            if (response.message.contains("ошибка", ignoreCase = true) || 
                response.message.contains("error", ignoreCase = true) || 
                response.message.contains("не удалось", ignoreCase = true)) {
                Log.e("RoadmapRepository", "Failed to update node status: ${response.message}")
                Result.failure(Exception(response.message))
            } else {
                // Log success but don't treat it as an error
                Log.d("RoadmapRepository", "Node status updated: ${response.message}")
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e("RoadmapRepository", "Error updating node status: ${e.message}")
            Result.failure(e)
        }
    }
}
