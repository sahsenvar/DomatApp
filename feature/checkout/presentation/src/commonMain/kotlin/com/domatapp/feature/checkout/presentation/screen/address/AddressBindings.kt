package com.domatapp.feature.checkout.presentation.screen.address

import androidx.compose.runtime.Composable
import com.domatapp.core.navigation.AddressNavigator
import com.domatapp.core.navigation.CheckoutGraph
import com.domatapp.core.presentation.screen.DomatEffectScope
import com.domatapp.core.presentation.screen.Effects
import com.domatapp.core.presentation.screen.ViewModelOf
import com.domatapp.feature.checkout.presentation.address.AddressEffect
import com.domatapp.feature.checkout.presentation.address.AddressIntent
import com.domatapp.feature.checkout.presentation.address.AddressViewModel
import org.koin.compose.viewmodel.koinViewModel

/** Fills `DomatScreenRoot`'s ViewModel slot for [CheckoutGraph.AddressRoute]. */
@ViewModelOf(CheckoutGraph.AddressRoute::class)
@Composable
fun addressViewModel(): AddressViewModel = koinViewModel()

/**
 * Both "Devam Et" and "Düzenle" push C4. The designed focus on C4's personal-info block for
 * "Düzenle" needs a `PaymentRoute` parameter that does not exist yet, so both use the same edge.
 * `scope` and `onIntent` are unused but required by the slot type (`SW8`).
 */
@Effects(CheckoutGraph.AddressRoute::class)
fun handleAddressEffect(
    effect: AddressEffect,
    scope: DomatEffectScope,
    onIntent: (AddressIntent) -> Unit,
    nav: AddressNavigator,
) {
    when (effect) {
        AddressEffect.NavigateBack -> nav.back()
        AddressEffect.NavigateToPayment -> nav.goToPayment()
        AddressEffect.NavigateToPaymentPersonalInfo -> nav.goToPayment()
    }
}
