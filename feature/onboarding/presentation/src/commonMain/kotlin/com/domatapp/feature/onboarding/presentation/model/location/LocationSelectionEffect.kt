package com.domatapp.feature.onboarding.presentation.model.location

sealed interface LocationSelectionEffect {
    data object NavigateToHome : LocationSelectionEffect
    data object NavigateBack : LocationSelectionEffect
}
