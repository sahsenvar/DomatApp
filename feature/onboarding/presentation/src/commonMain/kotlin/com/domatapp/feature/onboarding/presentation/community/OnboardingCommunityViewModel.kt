package com.domatapp.feature.onboarding.presentation.community

import com.domatapp.core.presentation.base.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class OnboardingCommunityViewModel : BaseViewModel<
    OnboardingCommunityUiState,
    OnboardingCommunityIntent,
    OnboardingCommunityEffect
>(OnboardingCommunityUiState()) {
    override fun onIntent(intent: OnboardingCommunityIntent) {
        when (intent) {
            OnboardingCommunityIntent.GoNext -> emitEffect(OnboardingCommunityEffect.NavigateToTrust)
            OnboardingCommunityIntent.GoBack -> emitEffect(OnboardingCommunityEffect.NavigateBack)
        }
    }
}
