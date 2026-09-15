package com.domatapp.feature.home.presentation.home

/**
 * Placeholder MVI contract for the signed-in landing screen.
 *
 * The screen has no behaviour yet, but it still needs a state/intent/effect triple: every route
 * rendered through `DomatScreenRoot` is resolved from a `BaseViewModel<S, I, E>`, and a `@Screen`
 * whose signature does not match the wrapper's content slot would fall back to an unwrapped entry.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
)

sealed interface HomeIntent

sealed interface HomeEffect
