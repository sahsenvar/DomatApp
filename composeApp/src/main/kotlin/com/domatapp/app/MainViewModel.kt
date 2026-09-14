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
                copy(backStack = backStack + intent.route)
            }

            is MainIntent.PopBack -> updateState {
                if (backStack.size > 1) copy(backStack = backStack.dropLast(1))
                else this
            }

            is MainIntent.PopBackTo -> updateState {
                val index = backStack.indexOfLast { it == intent.route }
                if (index >= 0) {
                    val removeFrom = if (intent.inclusive) index else index + 1
                    copy(backStack = backStack.take(removeFrom))
                } else {
                    this
                }
            }

            is MainIntent.ReplaceAll -> updateState {
                copy(backStack = listOf(intent.route))
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
