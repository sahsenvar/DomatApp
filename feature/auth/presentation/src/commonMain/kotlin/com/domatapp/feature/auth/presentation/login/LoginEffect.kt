package com.domatapp.feature.auth.presentation.login

sealed interface LoginEffect {
    /**
     * Ask the native UI to open the platform Google Sign-In dialog.
     */
    data object LaunchGoogleSignIn : LoginEffect

    /**
     * Existing user — go straight to the main app.
     */
    data object NavigateToHome : LoginEffect

    /**
     * New user — collect the delivery location before entering the app.
     */
    data object NavigateToLocationSelection : LoginEffect

    data class ShowError(val message: String) : LoginEffect
}
