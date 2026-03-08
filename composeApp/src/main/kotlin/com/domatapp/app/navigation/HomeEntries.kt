package com.domatapp.app.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.domatapp.core.navigation.Route
import com.domatapp.feature.home.presentation.screen.HomeScreen

fun EntryProviderScope<Route>.homeEntries() {
    entry<Route.Main.Home> {
        HomeScreen()
    }
}
