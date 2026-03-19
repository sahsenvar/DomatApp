package com.domatapp.feature.auth.presentation.model.login

sealed interface LoginEffect {
    data object NavigateToLocationSelection : LoginEffect
}
