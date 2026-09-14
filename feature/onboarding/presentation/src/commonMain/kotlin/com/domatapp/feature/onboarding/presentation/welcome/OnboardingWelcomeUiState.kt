package com.domatapp.feature.onboarding.presentation.welcome

data class OnboardingWelcomeUiState(
    val currentPage: OnboardingPage = OnboardingPage.WELCOME,
    val targetPage: OnboardingPage? = null,
)
