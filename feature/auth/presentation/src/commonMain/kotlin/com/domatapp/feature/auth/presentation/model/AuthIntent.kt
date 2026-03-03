package com.domatapp.feature.auth.presentation.model

/**
 * User intents/actions for Authentication screen.
 */
sealed class AuthIntent {
    /**
     * User clicked "Sign in with Google" button.
     * VM will emit LaunchGoogleSignIn effect.
     */
    data object OnGoogleSignInClicked : AuthIntent()

    /**
     * Native UI received Google ID token after successful sign-in.
     * VM will call LoginWithGoogleUseCase.
     */
    data class OnGoogleTokenReceived(val idToken: String) : AuthIntent()

    /**
     * User cancelled Google Sign-In dialog.
     */
    data object OnGoogleSignInCancelled : AuthIntent()

    /**
     * User clicked "Dismiss" on error message.
     */
    data object OnErrorDismissed : AuthIntent()
}
