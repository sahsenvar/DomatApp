package com.domatapp.feature.checkout.presentation.payment

import androidx.lifecycle.viewModelScope
import com.domatapp.core.presentation.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

/**
 * C4 - Ödeme.
 *
 * **Stub:** there is no checkout data layer yet, so the state starts from [PaymentStubData] and
 * [PaymentIntent.PayClicked] only simulates the request (loading on, then on to C5). The real flow -
 * `PATCH /v1/users/me` (first order), `POST /v1/payments/cards` (opt-in), `POST /v1/orders`, then
 * C5 ([PaymentEffect.OrderPlaced]) / [PaymentEffect.WindowClosed], or a [PaymentErrorUi] with the
 * CVV cleared on a decline - lands with `:feature:checkout:data`. The stub always "succeeds".
 */
@KoinViewModel
class PaymentViewModel : BaseViewModel<PaymentUiState, PaymentIntent, PaymentEffect>(
    PaymentStubData.initialState,
) {

    override fun onIntent(intent: PaymentIntent) {
        // While the payment is in flight every input is inert and back does nothing
        // (design/screens/C4/annotations.md → "Ödeme sürerken").
        if (currentState.isPaying) return

        when (intent) {
            is PaymentIntent.FirstNameChanged -> updateState { copy(firstName = intent.value) }
            is PaymentIntent.LastNameChanged -> updateState { copy(lastName = intent.value) }
            is PaymentIntent.EmailChanged -> updateState {
                // Clearing the e-mail hides the marketing consent and resets it.
                copy(
                    email = intent.value,
                    marketingConsent = marketingConsent && intent.value.isNotBlank(),
                )
            }
            is PaymentIntent.MarketingToggled -> updateState { copy(marketingConsent = intent.checked) }
            is PaymentIntent.SavedCardSelected -> updateState {
                copy(selectedCardId = intent.id, useNewCard = false, cvv = "")
            }
            is PaymentIntent.CvvChanged -> updateState {
                copy(cvv = intent.value.digitsOnly(PaymentUiState.CVV_MAX_LENGTH))
            }
            PaymentIntent.UseOtherCardClicked -> updateState {
                copy(useNewCard = true, selectedCardId = null, cvv = "", paymentError = null)
            }
            is PaymentIntent.CardNumberChanged -> updateState {
                copy(cardNumber = intent.value.digitsOnly(PaymentUiState.CARD_NUMBER_LENGTH))
            }
            is PaymentIntent.ExpiryChanged -> updateState {
                copy(cardExpiry = intent.value.digitsOnly(PaymentUiState.EXPIRY_LENGTH))
            }
            is PaymentIntent.SaveCardToggled -> updateState { copy(saveCard = intent.checked) }
            is PaymentIntent.ContractToggled -> updateState { copy(contractChecked = intent.checked) }
            PaymentIntent.ContractExpandToggled -> updateState { copy(contractExpanded = !contractExpanded) }
            PaymentIntent.PayClicked, PaymentIntent.RetryClicked -> pay()
            PaymentIntent.BackToCartClicked -> emitEffect(PaymentEffect.NavigateBackToCart)
            PaymentIntent.BackClicked -> emitEffect(PaymentEffect.NavigateBack)
        }
    }

    private fun pay() {
        if (!currentState.canPay) return
        updateState { copy(isPaying = true) }
        viewModelScope.launch {
            // TODO(C4): replace with the order use case once :feature:checkout:data exists.
            delay(STUB_PAYMENT_LATENCY_MS)
            updateState { copy(isPaying = false) }
            emitEffect(PaymentEffect.OrderPlaced(PaymentStubData.ORDER_ID))
        }
    }

    private fun String.digitsOnly(maxLength: Int): String = filter(Char::isDigit).take(maxLength)

    private companion object {
        const val STUB_PAYMENT_LATENCY_MS = 1_500L
    }
}
