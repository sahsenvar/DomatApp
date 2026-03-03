package com.domatapp.feature.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request DTO for Google Sign-In.
 */
@Serializable
data class GoogleSignInRequest(
    @SerialName("id_token")
    val idToken: String
)
