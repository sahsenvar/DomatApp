package com.domatapp.feature.checkout.presentation.screen.windowclosed

import androidx.compose.runtime.Composable
import com.domatapp.core.navigation.CheckoutGraph
import com.domatapp.core.navigation.WindowClosedNavigator
import com.domatapp.core.presentation.screen.DomatEffectScope
import com.domatapp.core.presentation.screen.Effects
import com.domatapp.core.presentation.screen.ViewModelOf
import com.domatapp.feature.checkout.presentation.windowclosed.WindowClosedEffect
import com.domatapp.feature.checkout.presentation.windowclosed.WindowClosedIntent
import com.domatapp.feature.checkout.presentation.windowclosed.WindowClosedViewModel
import org.koin.compose.viewmodel.koinViewModel

/** Fills `DomatScreenRoot`'s ViewModel slot for [CheckoutGraph.WindowClosedRoute]. */
@ViewModelOf(CheckoutGraph.WindowClosedRoute::class)
@Composable
fun windowClosedViewModel(): WindowClosedViewModel = koinViewModel()

/** `scope` and `onIntent` are unused but required by the slot type (`SW8`). */
@Effects(CheckoutGraph.WindowClosedRoute::class)
fun handleWindowClosedEffect(
    effect: WindowClosedEffect,
    scope: DomatEffectScope,
    onIntent: (WindowClosedIntent) -> Unit,
    nav: WindowClosedNavigator,
) {
    when (effect) {
        WindowClosedEffect.NavigateToMarket -> nav.replaceToHome()
    }
}
