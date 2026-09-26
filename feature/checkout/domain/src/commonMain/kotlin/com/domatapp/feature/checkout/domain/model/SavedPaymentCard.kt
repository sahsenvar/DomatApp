package com.domatapp.feature.checkout.domain.model

enum class PaymentCardBrand { Mastercard, Visa, Troy, Unknown }

/** A card the payment provider stored for the user (`GET /v1/payments/cards`). */
data class SavedPaymentCard(
    val id: String,
    val brand: PaymentCardBrand,
    val last4: String,
)
