package com.domatapp.app.scaffold

import com.domatapp.core.navigation.Route

data class ScaffoldConfig(
    val showTopBar: Boolean = false,
    val topBarTitle: String = "",
    val showBottomBar: Boolean = false,
    val showFab: Boolean = false
)

fun Route.scaffoldConfig(): ScaffoldConfig = when (this) {
    is Route.Auth.Login,
    is Route.Auth.Register,
    is Route.Auth.ForgotPassword -> ScaffoldConfig()

    is Route.Onboarding -> ScaffoldConfig()

    is Route.Main.Home -> ScaffoldConfig(
        showTopBar = true,
        topBarTitle = "Domat",
        showBottomBar = true,
        showFab = true
    )
    is Route.Main.Wallet -> ScaffoldConfig(
        showTopBar = true,
        topBarTitle = "Cüzdan",
        showBottomBar = true
    )
    is Route.Main.Notifications -> ScaffoldConfig(
        showTopBar = true,
        topBarTitle = "Bildirimler",
        showBottomBar = true
    )
    is Route.Main.Profile -> ScaffoldConfig(
        showTopBar = true,
        topBarTitle = "Profil",
        showBottomBar = true
    )

    is Route.Product.List -> ScaffoldConfig(
        showTopBar = true,
        topBarTitle = "Ürünler"
    )
    is Route.Product.Detail -> ScaffoldConfig(
        showTopBar = true,
        topBarTitle = "Ürün Detayı"
    )
}
