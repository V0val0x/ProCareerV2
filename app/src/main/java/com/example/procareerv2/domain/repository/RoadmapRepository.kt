package com.example.procareerv2.domain.repository

import com.example.procareerv2.domain.model.NodeStatus
import com.example.procareerv2.domain.model.Roadmap

interface RoadmapRepository {
    suspend fun getUserRoadmap(userId: Int): Result<Roadmap>
    suspend fun updateNodeStatus(userId: Int, nodeId: Int, status: NodeStatus): Result<Boolean>
}
