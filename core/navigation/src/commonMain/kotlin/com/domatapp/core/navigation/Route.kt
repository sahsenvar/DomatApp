package com.domatapp.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    @Serializable
    sealed interface Auth : Route {
        @Serializable data object Login : Auth
        @Serializable data object Register : Auth
        @Serializable data object ForgotPassword : Auth
    }

    @Serializable
    data object Onboarding : Route

    @Serializable
    data object Main : MainRoute, Route

    @Serializable
    sealed interface Product : Route {
        @Serializable data object List : Product
        @Serializable data class Detail(val productId: String) : Product
    }
}


