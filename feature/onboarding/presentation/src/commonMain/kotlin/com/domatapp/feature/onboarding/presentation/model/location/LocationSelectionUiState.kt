package com.domatapp.feature.onboarding.presentation.model.location

data class LocationSelectionUiState(
    val selectedBlock: String? = null,
    val selectedApartment: String? = null,
    val isConfirmEnabled: Boolean = false,
    val isBlokDropdownOpen: Boolean = false,
    val isDaireDropdownOpen: Boolean = false,
)
