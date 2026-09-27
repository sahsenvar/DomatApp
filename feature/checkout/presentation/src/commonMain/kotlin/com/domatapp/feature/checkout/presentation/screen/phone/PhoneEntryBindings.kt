package com.domatapp.feature.checkout.presentation.screen.phone

import androidx.compose.runtime.Composable
import com.domatapp.core.navigation.CheckoutGraph
import com.domatapp.core.navigation.PhoneEntryNavigator
import com.domatapp.core.presentation.screen.DomatEffectScope
import com.domatapp.core.presentation.screen.Effects
import com.domatapp.core.presentation.screen.ViewModelOf
import com.domatapp.feature.checkout.presentation.phone.PhoneEntryEffect
import com.domatapp.feature.checkout.presentation.phone.PhoneEntryIntent
import com.domatapp.feature.checkout.presentation.phone.PhoneEntryViewModel
import org.koin.compose.viewmodel.koinViewModel

/** Fills `DomatScreenRoot`'s ViewModel slot for [CheckoutGraph.PhoneEntryRoute]. */
@ViewModelOf(CheckoutGraph.PhoneEntryRoute::class)
@Composable
fun phoneEntryViewModel(): PhoneEntryViewModel = koinViewModel()

/**
 * Back returns to the cart (the cart is untouched). `scope` and `onIntent` are unused but must
 * stay: the slot type is `(E, DomatEffectScope, (I) -> Unit) -> Unit` (`SW8` otherwise).
 */
@Effects(CheckoutGraph.PhoneEntryRoute::class)
fun handlePhoneEntryEffect(
    effect: PhoneEntryEffect,
    scope: DomatEffectScope,
    onIntent: (PhoneEntryIntent) -> Unit,
    nav: PhoneEntryNavigator,
) {
    when (effect) {
        PhoneEntryEffect.NavigateBack -> nav.back()
        is PhoneEntryEffect.NavigateToOtp -> nav.goToOtpVerify(effect.phoneNumber)
    }
}
