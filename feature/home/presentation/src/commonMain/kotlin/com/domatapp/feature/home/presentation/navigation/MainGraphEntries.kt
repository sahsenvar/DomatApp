package com.domatapp.feature.home.presentation.navigation

import com.domatapp.feature.home.presentation.screen.provideHomeEntry
import dev.gezgin.core.compose.GezginEntryScope

fun GezginEntryScope.mainGraphEntries() {
    provideHomeEntry()
}
