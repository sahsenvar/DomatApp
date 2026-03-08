package com.domatapp.app.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.domatapp.core.navigation.Route
import com.domatapp.feature.auth.presentation.screen.AuthRoute

fun EntryProviderScope<Route>.authEntries() {
    entry<Route.Auth.Login> {
        AuthRoute()
    }
}
