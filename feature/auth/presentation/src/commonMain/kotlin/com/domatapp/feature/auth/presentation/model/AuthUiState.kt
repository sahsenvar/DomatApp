package com.domatapp.feature.auth.presentation.model

import com.domatapp.core.presentation.model.UiState

/**
 * UI State for Authentication screen.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
) : UiState
