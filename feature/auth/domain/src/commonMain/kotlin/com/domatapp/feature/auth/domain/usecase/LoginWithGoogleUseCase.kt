package com.domatapp.feature.auth.domain.usecase

import com.domatapp.feature.auth.domain.model.AuthSession
import com.domatapp.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

/**
 * Use case for Google Sign-In.
 * Returns Flow<AuthSession> that throws exception on error.
 */
@Factory
class LoginWithGoogleUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(idToken: String): Flow<AuthSession> =
        repository.loginWithGoogle(idToken)
}
