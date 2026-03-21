package com.domatapp.feature.onboarding.presentation.community

sealed interface OnboardingCommunityEffect {
    data object NavigateToTrust : OnboardingCommunityEffect
    data object NavigateBack : OnboardingCommunityEffect
}
