package com.domatapp.feature.onboarding.presentation.model.effortless

sealed interface OnboardingEffortlessEffect {
    data object NavigateToPricing : OnboardingEffortlessEffect
    data object NavigateBack : OnboardingEffortlessEffect
}
