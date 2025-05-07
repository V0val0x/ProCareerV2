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
    @SerializedName("interests") private val _interests: Any? = null
) {
    // Интересы могут приходить как список строк или как список объектов
    val interests: List<Interest>
        get() {
            return when (_interests) {
                is List<*> -> {
                    (_interests as List<*>).mapNotNull { item ->
                        when (item) {
                            is String -> Interest(id = 0, name = item) // Если строка
                            is Map<*, *> -> {
                                val id = (item["id"] as? Number)?.toInt() ?: 0
                                val name = item["name"] as? String ?: return@mapNotNull null
                                Interest(id = id, name = name)
                            }
                            is InterestDto -> item.toDomainInterest()
                            else -> null
                        }
                    }
                }
                else -> emptyList()
            }
        }
    fun toDomainUser(token: String): User {
        return User(
            id = id,
            name = name,
            email = email,
            token = token,
            profileImage = profileImage,
            position = position ?: grade,  // If position is null, use grade
            specialization = specialization,
            interests = interests // Теперь interests уже типа List<Interest>
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
