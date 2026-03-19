package com.domatapp.feature.auth.presentation.model.login

sealed interface LoginIntent {
    data object OnGoogleSignInClicked : LoginIntent
}
