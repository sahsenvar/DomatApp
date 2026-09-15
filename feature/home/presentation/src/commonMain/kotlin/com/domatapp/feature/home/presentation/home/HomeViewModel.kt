package com.domatapp.feature.home.presentation.home

import com.domatapp.core.presentation.base.BaseViewModel

class HomeViewModel : BaseViewModel<HomeUiState, HomeIntent, HomeEffect>(HomeUiState()) {

    // No intents yet - HomeIntent has no members, so there is nothing to branch on.
    override fun onIntent(intent: HomeIntent) = Unit
}
