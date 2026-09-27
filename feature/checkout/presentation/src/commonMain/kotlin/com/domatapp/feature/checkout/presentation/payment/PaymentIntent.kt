package com.domatapp.feature.checkout.presentation.payment

/** `design/screens/C4/card.yaml → actions`, plus the contract's "Metni oku" toggle. */
sealed interface PaymentIntent {
    data class FirstNameChanged(val value: String) : PaymentIntent
    data class LastNameChanged(val value: String) : PaymentIntent
    data class EmailChanged(val value: String) : PaymentIntent
    data class MarketingToggled(val checked: Boolean) : PaymentIntent
    data class SavedCardSelected(val id: String) : PaymentIntent
    data class CvvChanged(val value: String) : PaymentIntent
    data object UseOtherCardClicked : PaymentIntent
    data class CardNumberChanged(val value: String) : PaymentIntent
    data class ExpiryChanged(val value: String) : PaymentIntent
    data class SaveCardToggled(val checked: Boolean) : PaymentIntent
    data class ContractToggled(val checked: Boolean) : PaymentIntent
    data object ContractExpandToggled : PaymentIntent
    data object PayClicked : PaymentIntent
    data object RetryClicked : PaymentIntent
    data object BackToCartClicked : PaymentIntent
    data object BackClicked : PaymentIntent
}
