package com.domatapp.feature.auth.data.remote

import com.domatapp.core.domain.model.RequestModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for Supabase auth/v1/token?grant_type=id_token endpoint.
 */
@Serializable
data class GoogleSignInRemoteModel(
    val provider: String = "google",
    @SerialName("id_token") val idToken: String
) : RequestModel
