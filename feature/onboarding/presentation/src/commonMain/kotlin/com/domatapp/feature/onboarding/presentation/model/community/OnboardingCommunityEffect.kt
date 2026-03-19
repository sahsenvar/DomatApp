package com.domatapp.feature.onboarding.presentation.model.community

sealed interface OnboardingCommunityEffect {
    data object NavigateToTrust : OnboardingCommunityEffect
    data object NavigateBack : OnboardingCommunityEffect
}
