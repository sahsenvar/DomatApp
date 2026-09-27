package com.domatapp.feature.checkout.presentation.screen.otp

import androidx.compose.runtime.Composable
import com.domatapp.core.navigation.CheckoutGraph
import com.domatapp.core.navigation.OtpVerifyNavigator
import com.domatapp.core.presentation.screen.DomatEffectScope
import com.domatapp.core.presentation.screen.Effects
import com.domatapp.core.presentation.screen.ViewModelOf
import com.domatapp.feature.checkout.presentation.otp.OtpVerifyEffect
import com.domatapp.feature.checkout.presentation.otp.OtpVerifyIntent
import com.domatapp.feature.checkout.presentation.otp.OtpVerifyViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Fills `DomatScreenRoot`'s ViewModel slot for [CheckoutGraph.OtpVerifyRoute]; the number comes from the route. */
@ViewModelOf(CheckoutGraph.OtpVerifyRoute::class)
@Composable
fun otpVerifyViewModel(route: CheckoutGraph.OtpVerifyRoute): OtpVerifyViewModel =
    koinViewModel { parametersOf(route.phoneNumber) }

/**
 * Forward edges replace C1 + C2 (see `CheckoutGraph.OtpVerifyRoute`); "Numarayı Değiştir" is the
 * single-step back to C1. `scope` and `onIntent` are unused but required by the slot type (`SW8`).
 */
@Effects(CheckoutGraph.OtpVerifyRoute::class)
fun handleOtpVerifyEffect(
    effect: OtpVerifyEffect,
    scope: DomatEffectScope,
    onIntent: (OtpVerifyIntent) -> Unit,
    nav: OtpVerifyNavigator,
) {
    when (effect) {
        OtpVerifyEffect.NavigateBack -> nav.back()
        OtpVerifyEffect.NavigateToAddress -> nav.replaceToAddress()
        OtpVerifyEffect.NavigateToPayment -> nav.replaceToPayment()
        OtpVerifyEffect.NavigateToWindowClosed -> nav.replaceToWindowClosed()
    }
}
