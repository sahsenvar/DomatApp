package com.domatapp.feature.onboarding.presentation.viewmodel

import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.feature.onboarding.presentation.model.pricing.OnboardingPricingEffect
import com.domatapp.feature.onboarding.presentation.model.pricing.OnboardingPricingIntent
import com.domatapp.feature.onboarding.presentation.model.pricing.OnboardingPricingUiState
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
