package com.domatapp.feature.checkout.presentation.payment

/**
 * Navigation side effects of C4.
 *
 * `OrderPlaced` (→ C5) and `WindowClosed` are deliberately absent until those routes exist in
 * `CheckoutGraph`; see the KDoc on `CheckoutGraph.PaymentRoute`.
 */
sealed interface PaymentEffect {
    data object NavigateBack : PaymentEffect
    data object NavigateBackToCart : PaymentEffect
}
