package com.domatapp.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    @Serializable
    sealed interface AuthRoute : Route {
        @Serializable data object AuthScreen        : AuthRoute
        @Serializable data object Login             : AuthRoute
        @Serializable data object LocationSelection : AuthRoute

        @Serializable
        data class AddressValidationScreen(
            val address: String
        ) : AuthRoute
    }

    @Serializable
    sealed interface OnboardingRoute : Route {
        @Serializable data object Welcome           : OnboardingRoute
        @Serializable data object Effortless        : OnboardingRoute
        @Serializable data object Pricing           : OnboardingRoute
        @Serializable data object Community         : OnboardingRoute
        @Serializable data object Trust             : OnboardingRoute
    }

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


