package com.domatapp.feature.checkout.presentation.confirmation

/** `design/screens/C5/card.yaml → actions`. */
sealed interface OrderConfirmationIntent {
    data object ShareWhatsAppClicked : OrderConfirmationIntent
    data object BackToMarketClicked : OrderConfirmationIntent
}
