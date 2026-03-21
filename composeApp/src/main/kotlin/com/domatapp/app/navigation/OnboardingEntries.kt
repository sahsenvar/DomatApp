package com.domatapp.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.ui.NavDisplay
import com.domatapp.core.navigation.Route
import com.domatapp.feature.onboarding.presentation.navigation.onboardingWelcomeScreenEntry

fun EntryProviderScope<Route>.onboardingPresentationEntries() {
    onboardingWelcomeScreenEntry(
        metadata = NavDisplay.transitionSpec { EnterTransition.None togetherWith ExitTransition.None } +
                NavDisplay.popTransitionSpec { EnterTransition.None togetherWith ExitTransition.None },
    )
}
