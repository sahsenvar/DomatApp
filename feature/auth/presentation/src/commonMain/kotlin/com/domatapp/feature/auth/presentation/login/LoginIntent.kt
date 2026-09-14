package com.domatapp.feature.auth.presentation.login

sealed interface LoginIntent {
    /**
     * User tapped the "Sign in with Google" button.
     */
    data object OnGoogleSignInClicked : LoginIntent

    /**
     * Native UI finished the Google Sign-In flow and produced an idToken.
     */
    data class OnGoogleTokenReceived(val idToken: String) : LoginIntent

    /**
     * User dismissed the Google Sign-In dialog, or it failed.
     */
    data object OnGoogleSignInCancelled : LoginIntent
}
