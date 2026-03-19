package com.domatapp.feature.auth.presentation.model.location

sealed interface LocationSelectionIntent {
    data class SelectBlock(val block: String) : LocationSelectionIntent
    data class SelectApartment(val apartment: String) : LocationSelectionIntent
    data object ToggleBlokDropdown : LocationSelectionIntent
    data object ToggleDaireDropdown : LocationSelectionIntent
    data object Confirm : LocationSelectionIntent
    data object GoBack : LocationSelectionIntent
}
