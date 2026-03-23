package com.domatapp.feature.auth.data.remote

import com.domatapp.core.data.model.RemoteModel
import com.domatapp.core.mapping.annotations.MapTo
import com.domatapp.feature.auth.domain.model.AuthSessionDomainModel
import com.domatapp.feature.auth.domain.model.AuthUserDomainModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Supabase auth session response.
 */
@Serializable
@MapTo(AuthSessionDomainModel::class)
data class AuthSessionRemoteModel(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String?,
    val user: AuthUserRemoteModel
) : RemoteModel

/**
 * Supabase auth user within session response.
 */
@Serializable
@MapTo(AuthUserDomainModel::class)
data class AuthUserRemoteModel(
    val id: String,
    val email: String,
    @SerialName("user_metadata") val userMetadata: AuthUserMetadataRemoteModel? = null
) : RemoteModel

/**
 * User metadata from Supabase (Google profile info).
 * Not mapped to domain — kept for future use.
 */
@Serializable
data class AuthUserMetadataRemoteModel(
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)
