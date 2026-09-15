package com.domatapp.feature.onboarding.presentation.welcome

import com.domatapp.core.presentation.base.BaseViewModel
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class OnboardingWelcomeViewModel : BaseViewModel<
    OnboardingWelcomeUiState,
    OnboardingWelcomeIntent,
    OnboardingWelcomeEffect
>(OnboardingWelcomeUiState()) {
    override fun onIntent(intent: OnboardingWelcomeIntent) {
        when (intent) {
            OnboardingWelcomeIntent.OnContinueClicked -> {
                if (state.value.targetPage != null) return
                val next = state.value.currentPage.next()
                if (next != null) {
                    updateState { copy(targetPage = next) }
                } else {
                    emitEffect(OnboardingWelcomeEffect.NavigateToLogin)
                }
            }
            OnboardingWelcomeIntent.OnScrollConsumed ->
                updateState { copy(targetPage = null) }
            is OnboardingWelcomeIntent.OnPageChanged ->
                updateState { copy(currentPage = OnboardingPage.fromIndex(intent.page)) }
        }
    }
}
