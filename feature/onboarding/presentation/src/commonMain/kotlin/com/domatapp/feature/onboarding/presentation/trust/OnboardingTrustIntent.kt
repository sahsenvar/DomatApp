package com.domatapp.feature.onboarding.presentation.trust

sealed interface OnboardingTrustIntent {
    data object GoNext : OnboardingTrustIntent
    data object GoBack : OnboardingTrustIntent
}
