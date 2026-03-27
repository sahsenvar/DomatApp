package com.domatapp.feature.auth.presentation.location

import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationViewModel
import com.domatapp.core.presentation.base.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@NavigationViewModel(Route.AuthRoute.LocationSelection::class)
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
                    )
                }
            is LocationSelectionIntent.SelectApartment ->
                updateState {
                    it.copy(
                        selectedApartment = intent.apartment,
                        isConfirmEnabled = it.selectedBlock != null,
                    )
                }
            LocationSelectionIntent.Confirm -> emitEffect(LocationSelectionEffect.NavigateToHome)
            LocationSelectionIntent.GoBack -> emitEffect(LocationSelectionEffect.NavigateBack)
        }
    }
}
