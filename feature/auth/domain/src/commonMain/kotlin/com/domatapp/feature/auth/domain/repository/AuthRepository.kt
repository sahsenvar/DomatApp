package com.domatapp.feature.auth.domain.repository

import com.domatapp.feature.auth.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

/**
 * Auth repository interface.
 * Implemented in data layer.
 */
interface AuthRepository {

    /**
     * Login with Google ID token.
     * Returns Flow that emits AuthSession on success, throws exception on error.
     */
    fun loginWithGoogle(idToken: String): Flow<AuthSession>

    /**
     * Observe current auth session.
     * Emits null if no active session.
     */
    fun observeAuthSession(): Flow<AuthSession?>

    /**
     * Logout current user.
     */
    fun logout(): Flow<Unit>
}
