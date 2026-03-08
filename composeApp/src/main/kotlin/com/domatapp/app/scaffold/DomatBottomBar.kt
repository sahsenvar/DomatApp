package com.domatapp.app.scaffold

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.domatapp.core.navigation.Route
import com.domatapp.core.presentation.compose.LocalNavigator

@Composable
fun DomatBottomBar(
    currentRoute: Route
) {
    val navigator = LocalNavigator.current

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute is Route.Main.Home,
            onClick = { navigator.replaceAll(Route.Main.Home) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Ana Sayfa") }
        )
        NavigationBarItem(
            selected = currentRoute is Route.Main.Wallet,
            onClick = { navigator.replaceAll(Route.Main.Wallet) },
            icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Wallet") },
            label = { Text("Cüzdan") }
        )
        NavigationBarItem(
            selected = currentRoute is Route.Main.Notifications,
            onClick = { navigator.replaceAll(Route.Main.Notifications) },
            icon = { Icon(Icons.Filled.Notifications, contentDescription = "Notifications") },
            label = { Text("Bildirimler") }
        )
        NavigationBarItem(
            selected = currentRoute is Route.Main.Profile,
            onClick = { navigator.replaceAll(Route.Main.Profile) },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profil") }
        )
    }
}
