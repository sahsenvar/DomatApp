package com.domatapp.core.navigation.annotations

import com.domatapp.core.navigation.Route
import kotlin.reflect.KClass

/**
 * Marks a @Composable function as the UI screen for a specific Route.
 * The function signature MUST follow: fun Name(uiState: UiState, onIntent: (Intent) -> Unit)
 *
 * Used together with @NavigationViewModel (and optionally @NavigationEffectHandler)
 * to auto-generate the Route composable that wires ViewModel -> Screen.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class NavigationScreen(val route: KClass<out Route>)
