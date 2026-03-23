package com.domatapp.feature.auth.domain.usecase

import com.domatapp.feature.auth.domain.model.LoginResultDomainModel
import com.domatapp.feature.auth.domain.repository.AuthRepository
import com.domatapp.feature.auth.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for Google Sign-In.
 * Authenticates via Supabase, then checks if the user has an onboarding profile.
 */
@Factory
class LoginWithGoogleUseCase(
    @Provided private val authRepository: AuthRepository,
    @Provided private val userProfileRepository: UserProfileRepository
) {
    operator fun invoke(idToken: String): Flow<LoginResultDomainModel> = flow {
        val session = authRepository.loginWithGoogle(idToken).first()
        val hasProfile = userProfileRepository.hasOnboardingRecord(session.user.id).first()
        emit(LoginResultDomainModel(session = session, hasUserExist = !hasProfile))
    }
}
