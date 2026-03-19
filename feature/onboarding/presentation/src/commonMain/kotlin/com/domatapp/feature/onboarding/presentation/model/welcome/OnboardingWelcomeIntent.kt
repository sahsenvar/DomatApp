package com.domatapp.feature.onboarding.presentation.model.welcome

sealed interface OnboardingWelcomeIntent {
    data object OnContinueClicked : OnboardingWelcomeIntent
    data object OnScrollConsumed : OnboardingWelcomeIntent
    data class OnPageChanged(val page: Int) : OnboardingWelcomeIntent
}
