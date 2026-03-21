package com.domatapp.feature.auth.domain.model

import com.domatapp.core.domain.model.DomainModel

/**
 * Domain model representing an authenticated user session.
 */
data class AuthSession(
    val userId: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val accessToken: String,
    val refreshToken: String?
) : DomainModel
