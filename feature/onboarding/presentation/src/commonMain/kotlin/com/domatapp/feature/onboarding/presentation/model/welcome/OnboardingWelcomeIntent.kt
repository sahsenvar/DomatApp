package com.domatapp.feature.onboarding.presentation.model.welcome

sealed interface OnboardingWelcomeIntent {
    data object GoNext : OnboardingWelcomeIntent
}
