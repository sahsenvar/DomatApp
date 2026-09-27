package com.domatapp.feature.checkout.presentation.windowclosed

import com.domatapp.core.presentation.base.BaseViewModel
import org.koin.core.annotation.KoinViewModel

/**
 * Pencere Kapandı. Nothing is undone here - the user stays signed in and the cart stays on the
 * device; the only action is back to the market.
 */
@KoinViewModel
class WindowClosedViewModel : BaseViewModel<WindowClosedUiState, WindowClosedIntent, WindowClosedEffect>(
    WindowClosedUiState,
) {

    override fun onIntent(intent: WindowClosedIntent) {
        when (intent) {
            WindowClosedIntent.BackToMarketClicked -> emitEffect(WindowClosedEffect.NavigateToMarket)
        }
    }
}
