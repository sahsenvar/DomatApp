package com.domatapp.feature.onboarding.presentation.welcome

sealed interface OnboardingWelcomeEffect {
    data object NavigateToLogin : OnboardingWelcomeEffect
}
