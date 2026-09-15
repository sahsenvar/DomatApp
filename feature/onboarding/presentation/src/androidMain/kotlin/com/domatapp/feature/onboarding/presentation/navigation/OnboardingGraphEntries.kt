package com.domatapp.feature.onboarding.presentation.navigation

import com.domatapp.feature.onboarding.presentation.screen.welcome.provideOnboardingWelcomeEntry
import dev.gezgin.core.compose.GezginEntryScope

fun GezginEntryScope.onboardingGraphEntries() {
    provideOnboardingWelcomeEntry()
}
