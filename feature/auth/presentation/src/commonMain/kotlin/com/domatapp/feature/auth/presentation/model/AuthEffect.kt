package com.domatapp.feature.auth.presentation.model

/**
 * One-time side effects for Authentication screen.
 */
sealed class AuthEffect {
    /**
     * Launch native Google Sign-In flow.
     * Android: Google Sign-In Activity
     * iOS: Google Sign-In SDK
     */
    data object LaunchGoogleSignIn : AuthEffect()

    /**
     * Navigate to home screen after successful login.
     */
    data object NavigateToHome : AuthEffect()

    /**
     * Show error toast message.
     */
    data class ShowError(val message: String) : AuthEffect()
}
