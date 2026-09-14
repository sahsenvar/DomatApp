package com.domatapp.feature.onboarding.presentation.effortless

sealed interface OnboardingEffortlessEffect {
    data object NavigateToPricing : OnboardingEffortlessEffect
    data object NavigateBack : OnboardingEffortlessEffect
}
