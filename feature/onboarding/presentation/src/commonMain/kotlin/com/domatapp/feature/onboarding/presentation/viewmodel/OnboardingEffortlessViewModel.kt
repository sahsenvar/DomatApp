package com.domatapp.feature.onboarding.presentation.viewmodel

import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.feature.onboarding.presentation.model.effortless.OnboardingEffortlessEffect
import com.domatapp.feature.onboarding.presentation.model.effortless.OnboardingEffortlessIntent
import com.domatapp.feature.onboarding.presentation.model.effortless.OnboardingEffortlessUiState
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
