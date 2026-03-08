package com.domatapp.app.scaffold

import com.domatapp.core.navigation.Route

data class ScaffoldConfig(
    val showBottomBar: Boolean = false,
    val showFab: Boolean = false
)

fun Route.scaffoldConfig(): ScaffoldConfig = when (this) {
    is Route.Auth.Login,
    is Route.Auth.Register,
    is Route.Auth.ForgotPassword -> ScaffoldConfig()

    is Route.Onboarding -> ScaffoldConfig()

    is Route.Main.Home -> ScaffoldConfig(
        showBottomBar = true,
        showFab = true
    )
    is Route.Main.Wallet -> ScaffoldConfig(showBottomBar = true)
    is Route.Main.Notifications -> ScaffoldConfig(showBottomBar = true)
    is Route.Main.Profile -> ScaffoldConfig(showBottomBar = true)

    is Route.Product.List -> ScaffoldConfig()
    is Route.Product.Detail -> ScaffoldConfig()
}
