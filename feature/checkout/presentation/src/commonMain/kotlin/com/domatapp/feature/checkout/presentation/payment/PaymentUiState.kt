package com.domatapp.feature.checkout.presentation.payment

import com.domatapp.feature.checkout.domain.model.Money
import com.domatapp.feature.checkout.domain.model.OrderLine
import com.domatapp.feature.checkout.domain.model.SavedPaymentCard

/**
 * C4 - Ödeme. Fields follow `design/screens/C4/card.yaml → data.uiState`; the `show*` / `can*`
 * properties are the card's `visibleWhen` / `enabled` rules, kept here so the screen stays a pure
 * function of state.
 *
 * @property currentDeliveryCode the delivery this order joins (`#DAL1`), for the second-order banner.
 */
data class PaymentUiState(
    val isFirstOrder: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val marketingConsent: Boolean = false,
    val orderSequenceInWindow: Int = 1,
    val currentDeliveryCode: String = "",
    val deliveryCancellationDeadlinePassed: Boolean = false,
    val items: List<OrderLine> = emptyList(),
    val total: Money = Money.Zero,
    val savedCardEnabled: Boolean = false,
    val savedCards: List<SavedPaymentCard> = emptyList(),
    val selectedCardId: String? = null,
    val useNewCard: Boolean = false,
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cvv: String = "",
    val saveCard: Boolean = false,
    val contractChecked: Boolean = false,
    val contractExpanded: Boolean = false,
    val belowMinimum: Boolean = false,
    val minimumAmount: Money = Money.Zero,
    val isPaying: Boolean = false,
    val paymentError: PaymentErrorUi? = null,
) {
    val showSecondOrderBanner: Boolean get() = orderSequenceInWindow >= 2

    val showMarketingConsent: Boolean get() = email.isNotBlank()

    val showSavedCards: Boolean get() = savedCardEnabled && savedCards.isNotEmpty()

    /** "Farklı kart kullan" under the saved cards; hidden once the new-card form is open. */
    val showUseOtherCardLink: Boolean get() = showSavedCards && !useNewCard

    val showNewCardForm: Boolean get() = !showSavedCards || useNewCard

    val showSaveCardOption: Boolean get() = savedCardEnabled

    val inputsEnabled: Boolean get() = !isPaying

    private val personalInfoValid: Boolean
        get() = !isFirstOrder || (firstName.isNotBlank() && lastName.isNotBlank())

    private val paymentMethodValid: Boolean
        get() = if (showNewCardForm) {
            cardNumber.length == CARD_NUMBER_LENGTH && isExpiryValid(cardExpiry) && isCvvValid(cvv)
        } else {
            selectedCardId != null && isCvvValid(cvv)
        }

    /**
     * `PrimaryButton · pay` enabled rule: personal info + card/CVV valid + contract checked.
     * Independent of [isPaying] so the button keeps its enabled look under the loading spinner.
     */
    val canPay: Boolean
        get() = !belowMinimum && contractChecked && personalInfoValid && paymentMethodValid

    companion object {
        const val CARD_NUMBER_LENGTH = 16
        const val EXPIRY_LENGTH = 4
        const val CVV_MAX_LENGTH = 4
        private const val CVV_MIN_LENGTH = 3
        private const val MONTHS_IN_YEAR = 12

        private fun isCvvValid(cvv: String) = cvv.length in CVV_MIN_LENGTH..CVV_MAX_LENGTH

        private fun isExpiryValid(expiry: String): Boolean {
            if (expiry.length != EXPIRY_LENGTH) return false
            val month = expiry.take(2).toIntOrNull() ?: return false
            return month in 1..MONTHS_IN_YEAR
        }
    }
}

/**
 * A declined payment. [userMessage] is the plain-language reason from the backend;
 * after [REPEATED_FAILURE_THRESHOLD] consecutive failures the banner switches to
 * `c4_card_declined_3x_body` and puts "Farklı kart kullan" first.
 */
data class PaymentErrorUi(
    val userMessage: String,
    val consecutiveFailures: Int,
) {
    val isRepeated: Boolean get() = consecutiveFailures >= REPEATED_FAILURE_THRESHOLD

    companion object {
        const val REPEATED_FAILURE_THRESHOLD = 3
    }
}
