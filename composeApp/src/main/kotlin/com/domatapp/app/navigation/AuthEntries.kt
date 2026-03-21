package com.domatapp.app.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.domatapp.core.navigation.Route
import com.domatapp.feature.auth.presentation.navigation.locationSelectionScreenEntry
import com.domatapp.feature.auth.presentation.navigation.loginScreenEntry

fun EntryProviderScope<Route>.authPresentationEntries() {
    loginScreenEntry()
    locationSelectionScreenEntry()
}
