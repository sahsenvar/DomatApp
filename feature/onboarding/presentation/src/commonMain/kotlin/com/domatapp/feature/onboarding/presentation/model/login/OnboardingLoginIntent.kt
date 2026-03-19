package com.domatapp.feature.onboarding.presentation.model.login

sealed interface OnboardingLoginIntent {
    data object OnGoogleSignInClicked : OnboardingLoginIntent
}
