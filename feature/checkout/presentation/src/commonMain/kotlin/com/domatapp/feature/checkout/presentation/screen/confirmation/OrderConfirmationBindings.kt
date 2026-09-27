package com.domatapp.feature.checkout.presentation.screen.confirmation

import androidx.compose.runtime.Composable
import com.domatapp.core.navigation.CheckoutGraph
import com.domatapp.core.navigation.OrderConfirmationNavigator
import com.domatapp.core.presentation.screen.DomatEffectScope
import com.domatapp.core.presentation.screen.Effects
import com.domatapp.core.presentation.screen.ViewModelOf
import com.domatapp.feature.checkout.presentation.confirmation.OrderConfirmationEffect
import com.domatapp.feature.checkout.presentation.confirmation.OrderConfirmationIntent
import com.domatapp.feature.checkout.presentation.confirmation.OrderConfirmationViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Fills `DomatScreenRoot`'s ViewModel slot for [CheckoutGraph.OrderConfirmationRoute]. */
@ViewModelOf(CheckoutGraph.OrderConfirmationRoute::class)
@Composable
fun orderConfirmationViewModel(route: CheckoutGraph.OrderConfirmationRoute): OrderConfirmationViewModel =
    koinViewModel { parametersOf(route.orderId) }

/**
 * "Pazar'a Dön" replaces the whole stack with Home. The WhatsApp share content is Flow D/F scope
 * and not designed yet, so the effect is a no-op for now. `scope` and `onIntent` are unused but
 * required by the slot type (`SW8`).
 */
@Effects(CheckoutGraph.OrderConfirmationRoute::class)
fun handleOrderConfirmationEffect(
    effect: OrderConfirmationEffect,
    scope: DomatEffectScope,
    onIntent: (OrderConfirmationIntent) -> Unit,
    nav: OrderConfirmationNavigator,
) {
    when (effect) {
        OrderConfirmationEffect.NavigateToMarket -> nav.replaceToHome()
        OrderConfirmationEffect.ShareWhatsApp -> Unit // TODO(C5): WhatsApp share intent (Flow D/F).
    }
}
