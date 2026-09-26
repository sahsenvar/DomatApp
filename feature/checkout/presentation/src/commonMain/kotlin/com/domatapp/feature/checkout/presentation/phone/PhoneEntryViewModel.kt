package com.domatapp.feature.checkout.presentation.phone

import androidx.lifecycle.viewModelScope
import com.domatapp.core.presentation.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

/**
 * C1 - Telefon Numarası Girişi.
 *
 * **Stub:** there is no checkout/auth data layer for this flow yet, so [PhoneEntryIntent.SendCodeClicked]
 * only simulates the requests (loading on, then on to C2). The real flow -
 * `POST /v1/users/me/kvkk-consent`, then `POST /v1/auth/otp/request`, or an [PhoneEntryUiState.errorMessage]
 * on failure - lands with the data layer.
 */
@KoinViewModel
class PhoneEntryViewModel : BaseViewModel<PhoneEntryUiState, PhoneEntryIntent, PhoneEntryEffect>(
    PhoneEntryUiState(),
) {

    override fun onIntent(intent: PhoneEntryIntent) {
        // While the OTP request is in flight every input is inert (annotations: "girişler pasif").
        if (currentState.isSending) return

        when (intent) {
            is PhoneEntryIntent.PhoneChanged -> updateState {
                // Editing hides a previous error again; it reappears on blur / submit only.
                copy(
                    phoneDigits = intent.digits.filter(Char::isDigit).take(PhoneEntryUiState.PHONE_DIGITS),
                    showPhoneError = false,
                    errorMessage = null,
                )
            }
            PhoneEntryIntent.PhoneFocusLost -> updateState {
                copy(showPhoneError = phoneDigits.isNotEmpty() && !isPhoneValid)
            }
            is PhoneEntryIntent.KvkkToggled -> updateState { copy(kvkkChecked = intent.checked) }
            PhoneEntryIntent.KvkkExpandToggled -> updateState { copy(kvkkExpanded = !kvkkExpanded) }
            PhoneEntryIntent.SendCodeClicked -> sendCode()
            PhoneEntryIntent.BackClicked -> emitEffect(PhoneEntryEffect.NavigateBack)
        }
    }

    private fun sendCode() {
        if (!currentState.isPhoneValid) {
            updateState { copy(showPhoneError = true) }
            return
        }
        if (!currentState.canSend) return
        updateState { copy(isSending = true) }
        viewModelScope.launch {
            // TODO(C1): replace with the KVKK consent + OTP request use cases once the data layer exists.
            delay(STUB_REQUEST_LATENCY_MS)
            updateState { copy(isSending = false) }
            emitEffect(PhoneEntryEffect.NavigateToOtp(currentState.phoneDigits))
        }
    }

    private companion object {
        const val STUB_REQUEST_LATENCY_MS = 1_000L
    }
}
