package com.domatapp.feature.checkout.presentation.payment

import com.domatapp.feature.checkout.domain.model.Money
import com.domatapp.feature.checkout.domain.model.OrderLine

/**
 * Stand-in for what C4 loads on entry (`GET /v1/deliveries/current`, `GET /v1/payments/cards`,
 * `GET /v1/app-config`) until `:feature:checkout:data` exists. Values are the design package's
 * `sampleData` (design/screens/C4/card.yaml). Delete with the stub.
 */
internal object PaymentStubData {

    val items = listOf(
        OrderLine(productName = "Domates", quantityLabel = "3 kg", lineTotal = Money.ofLira(96)),
        OrderLine(productName = "Salatalık", quantityLabel = "2 kg", lineTotal = Money.ofLira(54)),
        OrderLine(productName = "Kuru Soğan", quantityLabel = "2 kg", lineTotal = Money.ofLira(38)),
    )

    val initialState = PaymentUiState(
        isFirstOrder = true,
        items = items,
        total = items.fold(Money.Zero) { sum, line -> sum + line.lineTotal },
        savedCardEnabled = true,
        minimumAmount = Money.ofLira(60),
    ).let { it.copy(belowMinimum = it.total < it.minimumAmount) }
}
