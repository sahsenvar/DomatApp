package com.domatapp.core.navigation

import kotlinx.serialization.Serializable

sealed interface MainRoute {
    @Serializable
    data object Home : MainRoute

    @Serializable
    data object Wallet : MainRoute

    @Serializable
    data object Notifications : MainRoute

    @Serializable
    data object Profile : MainRoute
}