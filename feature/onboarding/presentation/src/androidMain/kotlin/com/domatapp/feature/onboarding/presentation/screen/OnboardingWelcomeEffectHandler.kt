package com.domatapp.feature.onboarding.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationEffectHandler
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@NavigationEffectHandler(Route.OnboardingRoute.Welcome::class)
@Composable
fun OnboardingWelcomeEffectHandler(effectFlow: Flow<OnboardingWelcomeEffect>) {
    val navigator = LocalNavigator.current
    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                OnboardingWelcomeEffect.NavigateToLogin ->
                    navigator.navigate(Route.AuthRoute.Login)
            }
        }
    }
}
