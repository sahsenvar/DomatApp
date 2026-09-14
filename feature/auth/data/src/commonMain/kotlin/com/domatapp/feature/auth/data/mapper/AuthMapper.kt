package com.domatapp.feature.auth.data.mapper

import com.domatapp.core.resulting.error.RemoteError
import com.domatapp.feature.auth.domain.error.AuthError

/**
 * Map exceptions to AuthError domain errors.
 * RemoteError from core:remote is already mapped, we just need to handle feature-specific cases.
 */
fun Throwable.toAuthError(): AuthError {
    return when (this) {
        // Already an AuthError
        is AuthError -> this

        // Map RemoteError to AuthError based on status codes
        is RemoteError.ClientError -> {
            when (code) {
                401 -> AuthError.InvalidCredentials
                403 -> AuthError.AccountDisabled
                404 -> AuthError.UserNotFound
                409 -> AuthError.EmailAlreadyInUse
                else -> AuthError.Unknown(message, this)
            }
        }

        is RemoteError -> {
            // Other remote errors are wrapped as Unknown
            AuthError.Unknown(message ?: "Remote error occurred", this)
        }

        // Unknown exceptions
        else -> AuthError.Unknown(message ?: "Unknown error occurred", this)
    }
}
