package com.domatapp.feature.onboarding.presentation.model.login

sealed interface OnboardingLoginEffect {
    data object NavigateToLocationSelection : OnboardingLoginEffect
}
