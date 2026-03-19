package com.domatapp.feature.onboarding.presentation.model.welcome

sealed interface OnboardingWelcomeEffect {
    data object NavigateToLogin : OnboardingWelcomeEffect
}
