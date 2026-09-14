package com.domatapp.feature.onboarding.presentation.trust

import com.domatapp.core.presentation.base.BaseViewModel
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class OnboardingTrustViewModel : BaseViewModel<
    OnboardingTrustUiState,
    OnboardingTrustIntent,
    OnboardingTrustEffect
>(OnboardingTrustUiState()) {
    override fun onIntent(intent: OnboardingTrustIntent) {
        when (intent) {
            OnboardingTrustIntent.GoNext -> emitEffect(OnboardingTrustEffect.NavigateToLogin)
            OnboardingTrustIntent.GoBack -> emitEffect(OnboardingTrustEffect.NavigateBack)
        }
    }
}
