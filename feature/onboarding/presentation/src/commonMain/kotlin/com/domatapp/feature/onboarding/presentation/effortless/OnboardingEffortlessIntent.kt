package com.domatapp.feature.onboarding.presentation.effortless

sealed interface OnboardingEffortlessIntent {
    data object GoNext : OnboardingEffortlessIntent
    data object GoBack : OnboardingEffortlessIntent
}
