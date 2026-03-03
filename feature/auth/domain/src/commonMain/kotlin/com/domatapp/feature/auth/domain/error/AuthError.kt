package com.domatapp.feature.auth.domain.error

import com.domatapp.core.resulting.error.DomainError

/**
 * Auth-specific domain errors.
 * Extends from base DomainError.
 */
sealed class AuthError(message: String? = null, cause: Throwable? = null) : DomainError(message, cause) {
    data object InvalidCredentials : AuthError("Invalid credentials")
    data object UserNotFound : AuthError("User not found")
    data object EmailAlreadyInUse : AuthError("Email already in use")
    data object AccountDisabled : AuthError("Account has been disabled")
    data class Unknown(override val message: String?, override val cause: Throwable? = null) : AuthError(message, cause)
}
