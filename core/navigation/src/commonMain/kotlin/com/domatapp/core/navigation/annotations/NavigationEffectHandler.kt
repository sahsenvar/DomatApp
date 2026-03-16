package com.domatapp.core.navigation.annotations

import com.domatapp.core.navigation.Route
import kotlin.reflect.KClass

/**
 * Marks a @Composable function as the side-effect handler for a specific Route.
 * The function signature MUST follow: fun Name(effectFlow: Flow<Effect>)
 *
 * Optional: if not present for a Route, the generated Route composable
 * will NOT include effect handling.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class NavigationEffectHandler(val route: KClass<out Route>)
