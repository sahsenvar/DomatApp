package com.domatapp.feature.checkout.presentation.screen.payment

import androidx.compose.runtime.Composable
import com.domatapp.core.navigation.CheckoutGraph
import com.domatapp.core.navigation.PaymentNavigator
import com.domatapp.core.presentation.screen.DomatEffectScope
import com.domatapp.core.presentation.screen.Effects
import com.domatapp.core.presentation.screen.ViewModelOf
import com.domatapp.feature.checkout.presentation.payment.PaymentEffect
import com.domatapp.feature.checkout.presentation.payment.PaymentIntent
import com.domatapp.feature.checkout.presentation.payment.PaymentViewModel
import org.koin.compose.viewmodel.koinViewModel

/** Fills `DomatScreenRoot`'s ViewModel slot for [CheckoutGraph.PaymentRoute]. */
@ViewModelOf(CheckoutGraph.PaymentRoute::class)
@Composable
fun paymentViewModel(): PaymentViewModel = koinViewModel()

/**
 * Only the implicit `back()` exists on [PaymentNavigator] for now. "Sepete Dön" is designed as
 * `backTo(B3)`; the cart route does not exist yet and the cart is the entry directly under C4 in
 * the designed flow, so a single-step `back()` is the same move today.
 *
 * `scope` and `onIntent` are unused but must stay: the slot type is
 * `(E, DomatEffectScope, (I) -> Unit) -> Unit` and a mismatching handler fails to bind (`SW8`).
 */
@Effects(CheckoutGraph.PaymentRoute::class)
fun handlePaymentEffect(
    effect: PaymentEffect,
    scope: DomatEffectScope,
    onIntent: (PaymentIntent) -> Unit,
    nav: PaymentNavigator,
) {
    when (effect) {
        PaymentEffect.NavigateBack -> nav.back()
        PaymentEffect.NavigateBackToCart -> nav.back()
    }
}
