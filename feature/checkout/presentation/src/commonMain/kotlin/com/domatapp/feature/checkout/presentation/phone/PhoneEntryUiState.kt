package com.domatapp.feature.checkout.presentation.phone

/**
 * C1 - Telefon Numarası Girişi. Fields follow `design/screens/C1/card.yaml → data.uiState`.
 *
 * `isPhoneValid` is derived rather than stored so it can never disagree with [phoneDigits].
 * The format error is only *shown* once [showPhoneError] is set (focus left the field, or a submit
 * was attempted) - never while typing (design/screens/C1/annotations.md).
 *
 * @property phoneDigits digits only, at most 10 (the `+90` prefix is fixed).
 * @property errorMessage a server-side reason (OTP request failed); shown in the same slot as the
 *   format error and takes precedence over it.
 */
data class PhoneEntryUiState(
    val phoneDigits: String = "",
    val showPhoneError: Boolean = false,
    val kvkkChecked: Boolean = false,
    val kvkkExpanded: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
) {
    /** A Turkish mobile number: 10 digits starting with 5. */
    val isPhoneValid: Boolean
        get() = phoneDigits.length == PHONE_DIGITS && phoneDigits.first() == MOBILE_PREFIX

    val isPhoneErrorVisible: Boolean get() = errorMessage != null || (showPhoneError && !isPhoneValid)

    /** `PrimaryButton · cta` enabled rule: `phoneValid && kvkkChecked`, independent of [isSending]. */
    val canSend: Boolean get() = isPhoneValid && kvkkChecked

    val inputsEnabled: Boolean get() = !isSending

    companion object {
        const val PHONE_DIGITS = 10
        private const val MOBILE_PREFIX = '5'
    }
}
