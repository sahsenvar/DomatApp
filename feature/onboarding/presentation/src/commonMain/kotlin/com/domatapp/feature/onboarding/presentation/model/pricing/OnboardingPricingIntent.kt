package com.domatapp.feature.onboarding.presentation.model.pricing

sealed interface OnboardingPricingIntent {
    data object GoNext : OnboardingPricingIntent
    data object GoBack : OnboardingPricingIntent
}
