package com.domatapp.feature.onboarding.presentation.viewmodel

import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.feature.onboarding.presentation.model.community.OnboardingCommunityEffect
import com.domatapp.feature.onboarding.presentation.model.community.OnboardingCommunityIntent
import com.domatapp.feature.onboarding.presentation.model.community.OnboardingCommunityUiState
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
