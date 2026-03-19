package com.domatapp.feature.onboarding.presentation.model.effortless

sealed interface OnboardingEffortlessIntent {
    data object GoNext : OnboardingEffortlessIntent
    data object GoBack : OnboardingEffortlessIntent
}
