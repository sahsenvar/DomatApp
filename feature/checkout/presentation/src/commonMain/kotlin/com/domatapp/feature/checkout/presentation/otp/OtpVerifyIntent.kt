package com.domatapp.feature.checkout.presentation.otp

/** `design/screens/C2/card.yaml → actions`. */
sealed interface OtpVerifyIntent {
    data class CodeChanged(val code: String) : OtpVerifyIntent
    data object ResendClicked : OtpVerifyIntent
    data object ChangeNumberClicked : OtpVerifyIntent
    data object BackClicked : OtpVerifyIntent
}
