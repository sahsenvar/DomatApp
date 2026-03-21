package com.domatapp.feature.auth.presentation.location

data class LocationSelectionUiState(
    val selectedBlock: String? = null,
    val selectedApartment: String? = null,
    val isConfirmEnabled: Boolean = false,
    val isBlokDropdownOpen: Boolean = false,
    val isDaireDropdownOpen: Boolean = false,
    val blockItems: List<String> = listOf("A1", "A2", "A3"),
    val apartmentItems: List<String> = listOf("1", "2", "3", "4", "5", "6", "7", "8"),
)
