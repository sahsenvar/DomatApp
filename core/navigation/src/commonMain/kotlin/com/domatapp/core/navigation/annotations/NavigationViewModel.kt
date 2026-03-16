package com.domatapp.core.navigation.annotations

import com.domatapp.core.navigation.Route
import kotlin.reflect.KClass

/**
 * Marks a ViewModel class as the state holder for a specific Route.
 * The ViewModel MUST extend BaseViewModel<UiState, Intent, Effect>.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class NavigationViewModel(val route: KClass<out Route>)
