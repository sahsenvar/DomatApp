package com.domatapp.feature.auth.presentation.model

sealed class AuthEffect {
    data object LaunchGoogleSignIn : AuthEffect()
    data object NavigateToHome : AuthEffect()
    data object NavigateToAddressInput : AuthEffect()
    data class ShowError(val message: String) : AuthEffect()
}
