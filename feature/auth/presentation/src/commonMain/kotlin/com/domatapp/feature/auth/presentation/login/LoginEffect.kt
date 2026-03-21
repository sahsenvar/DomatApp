package com.domatapp.feature.auth.presentation.login

sealed interface LoginEffect {
    data object NavigateToLocationSelection : LoginEffect
}
