package com.domatapp.feature.checkout.presentation.address

import androidx.lifecycle.viewModelScope
import com.domatapp.core.presentation.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

/**
 * C3 - Adres Ekleme.
 *
 * **Stub:** the state starts from [AddressStubData] and [AddressIntent.ContinueClicked] only
 * simulates `POST /v1/addresses` before continuing to C4. The real save lands with
 * `:feature:checkout:data`.
 */
@KoinViewModel
class AddressViewModel : BaseViewModel<AddressUiState, AddressIntent, AddressEffect>(
    AddressStubData.initialState,
) {

    override fun onIntent(intent: AddressIntent) {
        if (currentState.isSaving) return

        when (intent) {
            is AddressIntent.BlockSelected -> updateState {
                copy(selectedBlockId = intent.id, isBlockNotFound = false, blockFreeText = "")
            }
            AddressIntent.BlockNotFoundSelected -> updateState {
                copy(selectedBlockId = null, isBlockNotFound = true)
            }
            is AddressIntent.BlockFreeTextChanged -> updateState { copy(blockFreeText = intent.value) }
            is AddressIntent.ApartmentChanged -> updateState { copy(apartmentNo = intent.value) }
            is AddressIntent.NoteChanged -> updateState { copy(deliveryNote = intent.value) }
            AddressIntent.ContinueClicked -> save()
            AddressIntent.EditInvoiceClicked -> emitEffect(AddressEffect.NavigateToPaymentPersonalInfo)
            AddressIntent.BackClicked -> emitEffect(AddressEffect.NavigateBack)
        }
    }

    private fun save() {
        if (!currentState.canContinue) return
        updateState { copy(isSaving = true) }
        viewModelScope.launch {
            // TODO(C3): replace with the save-address use case (POST /v1/addresses).
            delay(STUB_SAVE_LATENCY_MS)
            updateState { copy(isSaving = false) }
            emitEffect(AddressEffect.NavigateToPayment)
        }
    }

    private companion object {
        const val STUB_SAVE_LATENCY_MS = 800L
    }
}
