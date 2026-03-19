package com.domatapp.feature.onboarding.presentation.model.community

sealed interface OnboardingCommunityIntent {
    data object GoNext : OnboardingCommunityIntent
    data object GoBack : OnboardingCommunityIntent
}
