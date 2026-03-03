package com.domatapp.feature.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote DTO for user authentication response.
 */
@Serializable
data class RemoteUserDto(
    @SerialName("user_id")
    val userId: String,
    @SerialName("email")
    val email: String,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("photo_url")
    val photoUrl: String? = null,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String? = null
)
