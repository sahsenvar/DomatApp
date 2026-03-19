package com.domatapp.feature.onboarding.presentation.viewmodel

import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationViewModel
import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.feature.onboarding.presentation.model.login.OnboardingLoginEffect
import com.domatapp.feature.onboarding.presentation.model.login.OnboardingLoginIntent
import com.domatapp.feature.onboarding.presentation.model.login.OnboardingLoginUiState
import org.koin.android.annotation.KoinViewModel

@NavigationViewModel(Route.OnboardingRoute.Login::class)
@KoinViewModel
class OnboardingLoginViewModel : BaseViewModel<
    OnboardingLoginUiState,
    OnboardingLoginIntent,
    OnboardingLoginEffect
>(OnboardingLoginUiState()) {
    override fun onIntent(intent: OnboardingLoginIntent) {
        when (intent) {
            OnboardingLoginIntent.OnGoogleSignInClicked ->
                emitEffect(OnboardingLoginEffect.NavigateToLocationSelection)
        }
    }
}
