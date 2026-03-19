package com.domatapp.feature.onboarding.presentation.model.trust

sealed interface OnboardingTrustIntent {
    data object GoNext : OnboardingTrustIntent
    data object GoBack : OnboardingTrustIntent
}
