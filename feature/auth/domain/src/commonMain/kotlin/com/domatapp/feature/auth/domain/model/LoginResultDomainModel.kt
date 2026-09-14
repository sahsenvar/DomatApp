package com.domatapp.feature.auth.domain.model

import com.domatapp.core.domain.model.DomainModel

/**
 * Combined result of login + onboarding check.
 * Emitted by LoginWithGoogleUseCase.
 */
data class LoginResultDomainModel(
    val session: AuthSessionDomainModel,
    val hasUserExist: Boolean
) : DomainModel
