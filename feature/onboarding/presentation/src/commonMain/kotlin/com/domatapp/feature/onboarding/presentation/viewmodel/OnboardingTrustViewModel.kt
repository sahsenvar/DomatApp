package com.domatapp.feature.onboarding.presentation.viewmodel

import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationViewModel
import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.feature.onboarding.presentation.model.trust.OnboardingTrustEffect
import com.domatapp.feature.onboarding.presentation.model.trust.OnboardingTrustIntent
import com.domatapp.feature.onboarding.presentation.model.trust.OnboardingTrustUiState
import org.koin.android.annotation.KoinViewModel

@NavigationViewModel(Route.OnboardingRoute.Trust::class)
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
