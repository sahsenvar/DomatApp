package com.domatapp.app.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.domatapp.core.navigation.Route
import com.domatapp.feature.onboarding.presentation.navigation.locationSelectionScreenEntry
import com.domatapp.feature.onboarding.presentation.navigation.onboardingLoginScreenEntry
import com.domatapp.feature.onboarding.presentation.navigation.onboardingWelcomeScreenEntry

fun EntryProviderScope<Route>.onboardingPresentationEntries() {
    onboardingWelcomeScreenEntry()
    onboardingLoginScreenEntry()
    locationSelectionScreenEntry()
}
