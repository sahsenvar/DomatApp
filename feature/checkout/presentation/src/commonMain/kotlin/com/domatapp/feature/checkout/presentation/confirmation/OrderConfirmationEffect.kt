package com.domatapp.feature.checkout.presentation.confirmation

/** Side effects of C5 (`card.yaml → navigation`). */
sealed interface OrderConfirmationEffect {
    /** WhatsApp share intent - its content is Flow D/F scope and not designed yet. */
    data object ShareWhatsApp : OrderConfirmationEffect

    /** `replaceTo` Pazar (Home); the checkout back stack is cleared. */
    data object NavigateToMarket : OrderConfirmationEffect
}
