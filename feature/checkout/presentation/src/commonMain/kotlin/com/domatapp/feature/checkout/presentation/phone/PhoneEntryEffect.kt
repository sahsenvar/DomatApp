package com.domatapp.feature.checkout.presentation.phone

/** Navigation side effects of C1 (`card.yaml → navigation`). */
sealed interface PhoneEntryEffect {
    data object NavigateBack : PhoneEntryEffect

    /** The OTP was requested; C2 verifies [phoneNumber] (10 digits, no prefix). */
    data class NavigateToOtp(val phoneNumber: String) : PhoneEntryEffect
}
