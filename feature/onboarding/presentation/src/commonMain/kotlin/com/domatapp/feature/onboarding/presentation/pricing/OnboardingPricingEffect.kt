package com.domatapp.feature.onboarding.presentation.pricing

sealed interface OnboardingPricingEffect {
    data object NavigateToCommunity : OnboardingPricingEffect
    data object NavigateBack : OnboardingPricingEffect
}
