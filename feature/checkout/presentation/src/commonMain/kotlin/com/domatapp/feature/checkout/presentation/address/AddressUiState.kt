package com.domatapp.feature.checkout.presentation.address

import com.domatapp.feature.checkout.domain.model.SiteBlock

/**
 * C3 - Adres Ekleme. Fields follow `design/screens/C3/card.yaml → data.uiState`.
 *
 * @property isBlockNotFound "Listede bulamadım" picked: [blockFreeText] replaces [selectedBlockId].
 * @property fullName invoice name of a returning user; `null` on the first order.
 */
data class AddressUiState(
    val siteName: String = "",
    val blocks: List<SiteBlock> = emptyList(),
    val selectedBlockId: String? = null,
    val blockFreeText: String = "",
    val isBlockNotFound: Boolean = false,
    val apartmentNo: String = "",
    val deliveryNote: String = "",
    val isFirstOrder: Boolean = true,
    val fullName: String? = null,
    val isSaving: Boolean = false,
) {
    val selectedBlock: SiteBlock? get() = blocks.firstOrNull { it.id == selectedBlockId }

    private val blockChosen: Boolean
        get() = if (isBlockNotFound) blockFreeText.isNotBlank() else selectedBlock != null

    /** `PrimaryButton · cta` enabled rule: `blockSelected && apartmentFilled`. */
    val canContinue: Boolean get() = blockChosen && apartmentNo.isNotBlank()

    /** Invoice row: the pending note on a first order, the name + "Düzenle" otherwise. */
    val showInvoiceRow: Boolean get() = !isFirstOrder && fullName != null

    val inputsEnabled: Boolean get() = !isSaving
}
