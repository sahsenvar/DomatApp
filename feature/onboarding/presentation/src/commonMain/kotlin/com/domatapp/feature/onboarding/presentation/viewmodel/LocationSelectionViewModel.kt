package com.domatapp.feature.onboarding.presentation.viewmodel

import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationViewModel
import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.feature.onboarding.presentation.model.location.LocationSelectionEffect
import com.domatapp.feature.onboarding.presentation.model.location.LocationSelectionIntent
import com.domatapp.feature.onboarding.presentation.model.location.LocationSelectionUiState
import org.koin.android.annotation.KoinViewModel

@NavigationViewModel(Route.OnboardingRoute.LocationSelection::class)
@KoinViewModel
class LocationSelectionViewModel : BaseViewModel<
    LocationSelectionUiState,
    LocationSelectionIntent,
    LocationSelectionEffect
>(LocationSelectionUiState()) {
    override fun onIntent(intent: LocationSelectionIntent) {
        when (intent) {
            is LocationSelectionIntent.SelectBlock ->
                updateState {
                    it.copy(
                        selectedBlock = intent.block,
                        isConfirmEnabled = it.selectedApartment != null,
                        isBlokDropdownOpen = false,
                    )
                }
            is LocationSelectionIntent.SelectApartment ->
                updateState {
                    it.copy(
                        selectedApartment = intent.apartment,
                        isConfirmEnabled = it.selectedBlock != null,
                        isDaireDropdownOpen = false,
                    )
                }
            LocationSelectionIntent.ToggleBlokDropdown -> {
                val newOpen = !currentState.isBlokDropdownOpen
                updateState {
                    it.copy(
                        isBlokDropdownOpen = newOpen,
                        isDaireDropdownOpen = if (newOpen) false else it.isDaireDropdownOpen,
                    )
                }
            }
            LocationSelectionIntent.ToggleDaireDropdown -> {
                val newOpen = !currentState.isDaireDropdownOpen
                updateState {
                    it.copy(
                        isDaireDropdownOpen = newOpen,
                        isBlokDropdownOpen = if (newOpen) false else it.isBlokDropdownOpen,
                    )
                }
            }
            LocationSelectionIntent.Confirm -> emitEffect(LocationSelectionEffect.NavigateToHome)
            LocationSelectionIntent.GoBack -> emitEffect(LocationSelectionEffect.NavigateBack)
        }
    }
}
