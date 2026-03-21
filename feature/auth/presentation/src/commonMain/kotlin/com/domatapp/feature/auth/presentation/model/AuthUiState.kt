package com.domatapp.feature.auth.presentation.model

import com.domatapp.feature.auth.domain.model.AuthSession

/**
 * UI State for Authentication screen.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val session: AuthSession? = null,
    val error: String? = null,
    val isGoogleSignInInProgress: Boolean = false
)