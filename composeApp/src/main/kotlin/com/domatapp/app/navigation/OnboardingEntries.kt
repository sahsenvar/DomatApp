package com.domatapp.app.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.domatapp.core.navigation.Route
import com.domatapp.feature.onboarding.presentation.navigation.locationSelectionScreenEntry
import com.domatapp.feature.onboarding.presentation.navigation.onboardingCommunityScreenEntry
import com.domatapp.feature.onboarding.presentation.navigation.onboardingEffortlessScreenEntry
import com.domatapp.feature.onboarding.presentation.navigation.onboardingLoginScreenEntry
import com.domatapp.feature.onboarding.presentation.navigation.onboardingPricingScreenEntry
import com.domatapp.feature.onboarding.presentation.navigation.onboardingTrustScreenEntry
import com.domatapp.feature.onboarding.presentation.navigation.onboardingWelcomeScreenEntry

fun EntryProviderScope<Route>.onboardingPresentationEntries() {
    onboardingWelcomeScreenEntry()
    onboardingEffortlessScreenEntry()
    onboardingPricingScreenEntry()
    onboardingCommunityScreenEntry()
    onboardingTrustScreenEntry()
    onboardingLoginScreenEntry()
    locationSelectionScreenEntry()
}
