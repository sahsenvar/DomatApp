package com.domatapp.feature.auth.presentation.model.location

sealed interface LocationSelectionEffect {
    data object NavigateToHome : LocationSelectionEffect
    data object NavigateBack : LocationSelectionEffect
}
