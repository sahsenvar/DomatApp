package com.domatapp.feature.onboarding.presentation.model.welcome

sealed interface OnboardingWelcomeIntent {
    data object GoogleSignInClicked : OnboardingWelcomeIntent
    data class OnPageChanged(val page: Int) : OnboardingWelcomeIntent
}
