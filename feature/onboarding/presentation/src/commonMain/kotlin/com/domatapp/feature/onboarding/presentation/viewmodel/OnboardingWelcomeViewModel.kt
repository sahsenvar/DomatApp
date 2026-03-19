package com.domatapp.feature.onboarding.presentation.viewmodel

import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationViewModel
import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeEffect
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeIntent
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeUiState
import org.koin.android.annotation.KoinViewModel

@NavigationViewModel(Route.OnboardingRoute.Welcome::class)
@KoinViewModel
class OnboardingWelcomeViewModel : BaseViewModel<
    OnboardingWelcomeUiState,
    OnboardingWelcomeIntent,
    OnboardingWelcomeEffect
>(OnboardingWelcomeUiState()) {
    override fun onIntent(intent: OnboardingWelcomeIntent) {
        when (intent) {
            OnboardingWelcomeIntent.GoogleSignInClicked ->
                emitEffect(OnboardingWelcomeEffect.NavigateToLogin)
            is OnboardingWelcomeIntent.OnPageChanged ->
                updateState { it.copy(currentPage = intent.page) }
        }
    }
}
