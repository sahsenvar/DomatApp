package com.domatapp.feature.auth.domain.model

import com.domatapp.core.domain.model.DomainModel

/**
 * Domain model representing an authenticated user session from Supabase.
 */
data class AuthSessionDomainModel(
    val accessToken: String,
    val refreshToken: String?,
    val user: AuthUserDomainModel
) : DomainModel

/**
 * Domain model representing the authenticated user.
 */
data class AuthUserDomainModel(
    val id: String,
    val email: String
) : DomainModel
