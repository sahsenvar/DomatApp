package com.domatapp.feature.checkout.presentation.otp

import androidx.lifecycle.viewModelScope
import com.domatapp.core.presentation.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

/**
 * C2 - OTP Doğrulama for [phoneNumber] (10 digits, from `CheckoutGraph.OtpVerifyRoute`).
 *
 * **Stub:** the sixth digit starts a simulated verification that always succeeds and continues to
 * C3 (no saved address). The real flow - `POST /v1/auth/otp/verify`, `POST /v1/cart/merge`,
 * `GET /v1/addresses/me`, cooldown / attempts from `GET /v1/app-config` - lands with the data layer.
 */
@KoinViewModel
class OtpVerifyViewModel(
    @InjectedParam val phoneNumber: String,
) : BaseViewModel<OtpVerifyUiState, OtpVerifyIntent, OtpVerifyEffect>(
    OtpVerifyUiState(
        maskedPhone = maskTrPhone(phoneNumber),
        cooldownSeconds = STUB_RESEND_COOLDOWN_SECONDS,
        attemptsLeft = STUB_MAX_ATTEMPTS,
    ),
) {

    private var countdown: Job? = null

    init {
        startCountdown()
    }

    override fun onIntent(intent: OtpVerifyIntent) {
        // While verifying (and after success) the code, links and back are all inert.
        if (!currentState.codeEnabled) return

        when (intent) {
            is OtpVerifyIntent.CodeChanged -> onCodeChanged(intent.code)
            OtpVerifyIntent.ResendClicked -> resend()
            OtpVerifyIntent.ChangeNumberClicked, OtpVerifyIntent.BackClicked ->
                emitEffect(OtpVerifyEffect.NavigateBack)
        }
    }

    private fun onCodeChanged(input: String) {
        // After a wrong code, the next keystroke starts over: the code is cleared and the error goes.
        if (currentState.isError) {
            updateState { copy(code = "", isError = false) }
            return
        }
        val code = input.filter(Char::isDigit).take(OtpVerifyUiState.CODE_LENGTH)
        updateState { copy(code = code) }
        if (code.length == OtpVerifyUiState.CODE_LENGTH) verify()
    }

    private fun verify() {
        updateState { copy(isVerifying = true) }
        viewModelScope.launch {
            // TODO(C2): replace with verify + cart merge + address lookup use cases. A wrong code sets
            //  isError (attemptsLeft - 1); merge 409 → NavigateToWindowClosed; an existing address → C4.
            delay(STUB_VERIFY_LATENCY_MS)
            updateState { copy(isVerifying = false) }
            countdown?.cancel()
            emitEffect(OtpVerifyEffect.NavigateToAddress)
        }
    }

    private fun resend() {
        if (!currentState.canResend) return
        // TODO(C2): POST /v1/auth/otp/request again.
        updateState { copy(code = "", isError = false, cooldownSeconds = STUB_RESEND_COOLDOWN_SECONDS) }
        startCountdown()
    }

    private fun startCountdown() {
        countdown?.cancel()
        countdown = viewModelScope.launch {
            while (isActive && currentState.cooldownSeconds > 0) {
                delay(ONE_SECOND_MS)
                updateState { copy(cooldownSeconds = (cooldownSeconds - 1).coerceAtLeast(0)) }
            }
        }
    }

    private companion object {
        /** Stand-ins for `app-config` `otpResendCooldownSeconds` / `otpMaxAttempts`. */
        const val STUB_RESEND_COOLDOWN_SECONDS = 45
        const val STUB_MAX_ATTEMPTS = 5
        const val STUB_VERIFY_LATENCY_MS = 1_000L
        const val ONE_SECOND_MS = 1_000L
    }
}
