package com.domatapp.feature.onboarding.presentation.model.trust

sealed interface OnboardingTrustEffect {
    data object NavigateToLogin : OnboardingTrustEffect
    data object NavigateBack : OnboardingTrustEffect
}
