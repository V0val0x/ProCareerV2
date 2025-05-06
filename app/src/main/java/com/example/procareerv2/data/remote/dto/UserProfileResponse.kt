package com.example.procareerv2.data.remote.dto

import com.example.procareerv2.domain.model.Interest
import com.example.procareerv2.domain.model.User
import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("profile_image") val profileImage: String? = null,
    @SerializedName("position") val position: String? = null,
    @SerializedName("grade") val grade: String? = null,
    @SerializedName("specialization") val specialization: String? = null,
    @SerializedName("interests") val interests: List<InterestDto> = emptyList()
) {
    fun toDomainUser(token: String): User {
        return User(
            id = id,
            name = name,
            email = email,
            token = token,
            profileImage = profileImage,
            position = position ?: grade,  // If position is null, use grade
            specialization = specialization,
            interests = interests.map { it.toDomainInterest() }
        )
    }
}

data class InterestDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
) {
    fun toDomainInterest(): Interest {
        return Interest(
            id = id,
            name = name
        )
    }
}
