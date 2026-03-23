package com.domatapp.feature.auth.domain.repository

import com.domatapp.feature.auth.domain.model.AuthSessionDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * Auth repository interface.
 * Implemented in data layer.
 */
interface AuthRepository {

    /**
     * Login with Google ID token via Supabase auth endpoint.
     * Returns Flow that emits AuthSessionDomainModel on success, throws exception on error.
     */
    fun loginWithGoogle(idToken: String): Flow<AuthSessionDomainModel>

    /**
     * Logout current user.
     */
    fun logout(): Flow<Unit>
}
