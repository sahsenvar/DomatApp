package com.domatapp.feature.checkout.presentation.windowclosed

/** Pencere Kapandı has no state (`design/screens/WindowClosed/card.yaml → data.uiState: []`). */
data object WindowClosedUiState

/** `card.yaml → actions`. */
sealed interface WindowClosedIntent {
    data object BackToMarketClicked : WindowClosedIntent
}

/** `card.yaml → navigation`: `replaceTo` Pazar (Home). */
sealed interface WindowClosedEffect {
    data object NavigateToMarket : WindowClosedEffect
}
