package com.domatapp.feature.checkout.presentation.address

/** `design/screens/C3/card.yaml → actions`. */
sealed interface AddressIntent {
    data class BlockSelected(val id: String) : AddressIntent
    data object BlockNotFoundSelected : AddressIntent
    data class BlockFreeTextChanged(val value: String) : AddressIntent
    data class ApartmentChanged(val value: String) : AddressIntent
    data class NoteChanged(val value: String) : AddressIntent
    data object ContinueClicked : AddressIntent
    data object EditInvoiceClicked : AddressIntent
    data object BackClicked : AddressIntent
}
