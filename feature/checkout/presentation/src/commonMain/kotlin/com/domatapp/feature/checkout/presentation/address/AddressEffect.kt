package com.domatapp.feature.checkout.presentation.address

/** Navigation side effects of C3 (`card.yaml → navigation`). */
sealed interface AddressEffect {
    data object NavigateBack : AddressEffect

    /** Address saved → C4. */
    data object NavigateToPayment : AddressEffect

    /**
     * "Düzenle" on the invoice row → C4, focused on the personal-info block. The focus request is
     * not carried yet: `CheckoutGraph.PaymentRoute` has no parameter for it.
     */
    data object NavigateToPaymentPersonalInfo : AddressEffect
}
