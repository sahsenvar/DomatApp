package com.domatapp.app

import com.domatapp.core.navigation.Route

data class MainUiState(
    val backStack: List<Route> = listOf(Route.Auth.Login)
) {
    val currentRoute: Route get() = backStack.last()
}