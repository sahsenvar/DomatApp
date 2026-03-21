package com.domatapp.feature.auth.presentation.login

sealed interface LoginIntent {
    data object OnGoogleSignInClicked : LoginIntent
}
