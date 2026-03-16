package com.domatapp.core.navigation.annotations

import com.domatapp.core.navigation.Route
import kotlin.reflect.KClass

/**
 * Marks a @Composable function as the bottom bar for a specific Route.
 * The function signature MUST follow: fun Name(uiState: UiState, onIntent: (Intent) -> Unit)
 *
 * Optional: if present, the generated entry composable places
 * this function at the bottom of the outer Column, after the screen content.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class BottomBar(val route: KClass<out Route>)
