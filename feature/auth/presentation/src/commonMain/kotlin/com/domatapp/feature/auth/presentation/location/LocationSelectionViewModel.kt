package com.domatapp.feature.auth.presentation.location

import com.domatapp.core.presentation.base.BaseViewModel
import org.koin.core.annotation.KoinViewModel

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
                    copy(
                        selectedBlock = intent.block,
                        isConfirmEnabled = selectedApartment != null,
                    )
                }
            is LocationSelectionIntent.SelectApartment ->
                updateState {
                    copy(
                        selectedApartment = intent.apartment,
                        isConfirmEnabled = selectedBlock != null,
                    )
                }
            LocationSelectionIntent.Confirm -> emitEffect(LocationSelectionEffect.NavigateToHome)
            LocationSelectionIntent.GoBack -> emitEffect(LocationSelectionEffect.NavigateBack)
        }
    }
}
