package com.domatapp.feature.onboarding.presentation.pricing

import com.domatapp.core.presentation.base.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class OnboardingPricingViewModel : BaseViewModel<
    OnboardingPricingUiState,
    OnboardingPricingIntent,
    OnboardingPricingEffect
>(OnboardingPricingUiState()) {
    override fun onIntent(intent: OnboardingPricingIntent) {
        when (intent) {
            OnboardingPricingIntent.GoNext -> emitEffect(OnboardingPricingEffect.NavigateToCommunity)
            OnboardingPricingIntent.GoBack -> emitEffect(OnboardingPricingEffect.NavigateBack)
        }
    }
}
