package com.domatapp.feature.checkout.presentation.otp

/**
 * C2 - OTP Doğrulama. Fields follow `design/screens/C2/card.yaml → data.uiState`.
 *
 * @property maskedPhone `+90 532 *** ** 67` - see [maskTrPhone].
 * @property cooldownSeconds seconds until "Kodu Tekrar Gönder" is allowed; 0 = available.
 * @property showLinkedAccountNote verified and attached to an existing account; shown briefly
 *   before navigating on.
 */
data class OtpVerifyUiState(
    val maskedPhone: String = "",
    val code: String = "",
    val isVerifying: Boolean = false,
    val isError: Boolean = false,
    val cooldownSeconds: Int = 0,
    val attemptsLeft: Int = 0,
    val showLinkedAccountNote: Boolean = false,
) {
    /** Cells are inert while verifying and once verified (linked-account note on screen). */
    val codeEnabled: Boolean get() = !isVerifying && !showLinkedAccountNote

    /** The resend row belongs to the entry phase; it is gone while verifying / after success. */
    val showResend: Boolean get() = codeEnabled

    val canResend: Boolean get() = cooldownSeconds == 0

    val showCountdown: Boolean get() = cooldownSeconds > 0

    companion object {
        const val CODE_LENGTH = 6
    }
}

/**
 * `5321234567` → `+90 532 *** ** 67`: area code and last two digits stay readable, the rest is
 * masked. Anything that is not a 10-digit number is returned with the prefix only.
 */
fun maskTrPhone(digits: String): String {
    if (digits.length != TR_PHONE_LENGTH) return "+90 $digits"
    return "+90 ${digits.take(AREA_CODE_LENGTH)} *** ** ${digits.takeLast(VISIBLE_TAIL)}"
}

private const val TR_PHONE_LENGTH = 10
private const val AREA_CODE_LENGTH = 3
private const val VISIBLE_TAIL = 2
