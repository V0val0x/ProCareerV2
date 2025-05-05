package com.example.procareerv2.data.remote.dto

import com.example.procareerv2.domain.model.Interest
import com.example.procareerv2.domain.model.User
import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: UserProfileData
)

data class UserProfileData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("profile_image") val profileImage: String?,
    @SerializedName("position") val position: String?,
    @SerializedName("interests") val interests: List<InterestDto> = emptyList()
) {
    fun toDomainUser(token: String): User {
        return User(
            id = id,
            name = name,
            email = email,
            token = token,
            profileImage = profileImage,
            position = position,
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
