package com.domatapp.feature.checkout.presentation.payment

/** Navigation side effects of C4 (`design/screens/C4/card.yaml → navigation`). */
sealed interface PaymentEffect {
    data object NavigateBack : PaymentEffect
    data object NavigateBackToCart : PaymentEffect

    /** `POST /v1/orders` 2xx → C5 (replaces C4). */
    data class OrderPlaced(val orderId: String) : PaymentEffect

    /** `POST /v1/orders` answered `409 window_closed`. */
    data object WindowClosed : PaymentEffect
}
