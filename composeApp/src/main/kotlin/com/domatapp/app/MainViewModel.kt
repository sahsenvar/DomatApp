package com.domatapp.app

import com.domatapp.core.navigation.Navigator
import com.domatapp.core.navigation.Route
import com.domatapp.core.presentation.base.BaseViewModel

class MainViewModel : BaseViewModel<MainUiState, MainIntent, MainEffect>(
    initialState = MainUiState()
), Navigator {

    override fun onIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.Navigate -> updateState {
                it.copy(backStack = it.backStack + intent.route)
            }

            is MainIntent.PopBack -> updateState { state ->
                if (state.backStack.size > 1) {
                    state.copy(backStack = state.backStack.dropLast(1))
                } else {
                    state
                }
            }

            is MainIntent.PopBackTo -> updateState { state ->
                val index = state.backStack.indexOfLast { it == intent.route }
                if (index >= 0) {
                    val removeFrom = if (intent.inclusive) index else index + 1
                    state.copy(backStack = state.backStack.take(removeFrom))
                } else {
                    state
                }
            }

            is MainIntent.ReplaceAll -> updateState {
                it.copy(backStack = listOf(intent.route))
            }
        }
    }

    // Navigator interface delegates to MVI intents
    override fun navigate(route: Route) = onIntent(MainIntent.Navigate(route))
    override fun popBack() = onIntent(MainIntent.PopBack)
    override fun popBackTo(route: Route, inclusive: Boolean) =
        onIntent(MainIntent.PopBackTo(route, inclusive))
    override fun replaceAll(route: Route) = onIntent(MainIntent.ReplaceAll(route))
}
