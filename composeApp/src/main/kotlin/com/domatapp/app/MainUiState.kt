package com.domatapp.app

import com.domatapp.core.navigation.Route

data class MainUiState(
    val backStack: List<Route> = listOf(Route.AuthRoute.AuthScreen)
) {
    val currentRoute: Route get() = backStack.last()
}