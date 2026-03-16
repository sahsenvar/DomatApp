package com.domatapp.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    @Serializable
    sealed interface AuthRoute : Route {
        @Serializable
        data object AuthScreen : AuthRoute

        @Serializable
        data class AddressValidationScreen(
            val address: String
        ) : AuthRoute
    }

    @Serializable
    data object Onboarding : Route

    @Serializable
    sealed interface Main : Route {
        @Serializable
        data object Home : Main

        @Serializable
        data object Wallet : Main

        @Serializable
        data object Notifications : Main

        @Serializable
        data object Profile : Main
    }

    @Serializable
    sealed interface Product : Route {
        @Serializable data object List : Product
        @Serializable data class Detail(val productId: String) : Product
    }
}


