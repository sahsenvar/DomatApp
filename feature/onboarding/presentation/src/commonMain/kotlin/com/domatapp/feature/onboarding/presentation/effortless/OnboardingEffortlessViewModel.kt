package com.domatapp.feature.onboarding.presentation.effortless

import com.domatapp.core.presentation.base.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class OnboardingEffortlessViewModel : BaseViewModel<
    OnboardingEffortlessUiState,
    OnboardingEffortlessIntent,
    OnboardingEffortlessEffect
>(OnboardingEffortlessUiState()) {
    override fun onIntent(intent: OnboardingEffortlessIntent) {
        when (intent) {
            OnboardingEffortlessIntent.GoNext -> emitEffect(OnboardingEffortlessEffect.NavigateToPricing)
            OnboardingEffortlessIntent.GoBack -> emitEffect(OnboardingEffortlessEffect.NavigateBack)
        }
    }
}
