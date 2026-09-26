package com.domatapp.feature.checkout.presentation.otp

/** Navigation side effects of C2 (`card.yaml → navigation`). */
sealed interface OtpVerifyEffect {
    /** "Numarayı Değiştir" and system back both return to C1. */
    data object NavigateBack : OtpVerifyEffect

    /** Verified + cart merged, the user has no address yet → C3. */
    data object NavigateToAddress : OtpVerifyEffect

    /** Verified + cart merged, an address exists → C4. */
    data object NavigateToPayment : OtpVerifyEffect

    /** `POST /v1/cart/merge` answered `409 window_closed`. */
    data object NavigateToWindowClosed : OtpVerifyEffect
}
