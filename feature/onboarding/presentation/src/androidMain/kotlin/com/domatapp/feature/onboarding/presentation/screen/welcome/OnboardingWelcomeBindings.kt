package com.domatapp.feature.onboarding.presentation.screen.welcome

import androidx.compose.runtime.Composable
import com.domatapp.core.navigation.OnboardingGraph
import com.domatapp.core.navigation.OnboardingWelcomeNavigator
import com.domatapp.core.presentation.screen.DomatEffectScope
import com.domatapp.core.presentation.screen.Effects
import com.domatapp.core.presentation.screen.ViewModelOf
import com.domatapp.feature.onboarding.presentation.welcome.OnboardingWelcomeEffect
import com.domatapp.feature.onboarding.presentation.welcome.OnboardingWelcomeIntent
import com.domatapp.feature.onboarding.presentation.welcome.OnboardingWelcomeViewModel
import org.koin.compose.viewmodel.koinViewModel

/** Fills `DomatScreenRoot`'s ViewModel slot for [OnboardingGraph.OnboardingWelcomeRoute]. */
@ViewModelOf(OnboardingGraph.OnboardingWelcomeRoute::class)
@Composable
fun onboardingWelcomeViewModel(): OnboardingWelcomeViewModel = koinViewModel()

/**
 * `goToLogin()` is the only forward edge this route declares, and therefore the only navigation
 * method [OnboardingWelcomeNavigator] has.
 */
@Effects(OnboardingGraph.OnboardingWelcomeRoute::class)
fun handleOnboardingWelcomeEffect(
    effect: OnboardingWelcomeEffect,
    scope: DomatEffectScope,
    onIntent: (OnboardingWelcomeIntent) -> Unit,
    nav: OnboardingWelcomeNavigator,
) {
    when (effect) {
        OnboardingWelcomeEffect.NavigateToLogin -> nav.goToLogin()
    }
}
