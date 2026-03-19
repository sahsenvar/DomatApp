package com.domatapp.feature.onboarding.presentation.model.pricing

sealed interface OnboardingPricingEffect {
    data object NavigateToCommunity : OnboardingPricingEffect
    data object NavigateBack : OnboardingPricingEffect
}
