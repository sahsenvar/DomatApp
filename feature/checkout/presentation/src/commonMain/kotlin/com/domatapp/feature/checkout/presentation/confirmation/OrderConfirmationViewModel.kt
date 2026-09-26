package com.domatapp.feature.checkout.presentation.confirmation

import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.feature.checkout.domain.model.Money
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

/**
 * C5 - Sipariş Onay for [orderId] (from `CheckoutGraph.OrderConfirmationRoute`).
 *
 * **Stub:** the state is the design package's `sampleData` (design/screens/C5/card.yaml) until
 * `GET /v1/orders/{orderId}` and `GET /v1/deliveries/current` are wired with `:feature:checkout:data`.
 */
@KoinViewModel
class OrderConfirmationViewModel(
    @InjectedParam val orderId: String,
) : BaseViewModel<OrderConfirmationUiState, OrderConfirmationIntent, OrderConfirmationEffect>(
    // TODO(C5): load the order ([orderId]) and the current delivery instead of the sample values.
    OrderConfirmationUiState(
        deliveryCode = "#DAL1",
        deliveryDay = "Cumartesi, 3 Ekim",
        blockedAmount = Money.ofLira(STUB_BLOCKED_LIRA),
        communityProgress = STUB_COMMUNITY_PROGRESS,
        communityCaption = "Bir sonraki kademeye 12 sipariş kaldı — kg fiyatı ₺2 daha düşecek.",
    ),
) {

    override fun onIntent(intent: OrderConfirmationIntent) {
        when (intent) {
            OrderConfirmationIntent.ShareWhatsAppClicked -> emitEffect(OrderConfirmationEffect.ShareWhatsApp)
            OrderConfirmationIntent.BackToMarketClicked -> emitEffect(OrderConfirmationEffect.NavigateToMarket)
        }
    }

    private companion object {
        const val STUB_BLOCKED_LIRA = 188L
        const val STUB_COMMUNITY_PROGRESS = 0.5f
    }
}
