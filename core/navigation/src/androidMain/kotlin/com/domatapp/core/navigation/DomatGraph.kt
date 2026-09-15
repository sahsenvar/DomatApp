package com.domatapp.core.navigation

import dev.gezgin.core.Route
import dev.gezgin.core.annotation.GoTo
import dev.gezgin.core.annotation.NavGraph
import dev.gezgin.core.annotation.ReplaceTo

/**
 * The application's navigation graph.
 *
 * Every edge below is declared on the route it starts from, and Gezgin's KSP processor turns each
 * one into a single method on that route's generated `<X>Navigator`. A screen can therefore only
 * navigate along the edges its own route declares - `nav.goToLogin()` exists on
 * `OnboardingWelcomeNavigator` because [OnboardingGraph.OnboardingWelcomeRoute] declares
 * `@GoTo(LoginRoute)`, and there is no method at all for anywhere it did not declare.
 *
 * Route types carry no `@Serializable`: Gezgin generates and registers their serializers itself
 * (`gezginSerializersModule` / `gezginJson`). A route that gains a parameter of a project-defined
 * type would need `@Serializable` on that parameter type, not on the route.
 *
 * Naming: the generated names strip a trailing `Route` and then a trailing `Screen`/`Flow`, so
 * `LoginRoute` yields `LoginNavigator`, `provideLoginEntry()` and `goToLogin()`.
 */
@NavGraph
sealed interface OnboardingGraph : Route {

    /**
     * The app's start destination - see `rememberNavigator(start = ...)` in `MainActivity`.
     *
     * Because it is always the bottom of the stack, it is also the `clearUpTo` anchor the auth
     * routes use to wipe the funnel on a successful sign-in.
     */
    @GoTo(AuthGraph.LoginRoute::class)
    data object OnboardingWelcomeRoute : OnboardingGraph
}

@NavGraph
sealed interface AuthGraph : Route {

    /**
     * `goToLocationSelection()` for a brand-new user, `replaceToHome()` for a returning one.
     *
     * The replace clears up to and including the start destination, which is what the old
     * hand-rolled `Navigator.replaceAll(Route.Main.Home)` did: after a successful login the
     * onboarding/auth funnel must not be reachable by system or predictive back.
     */
    @GoTo(LocationSelectionRoute::class)
    @ReplaceTo(
        target = MainGraph.HomeRoute::class,
        clearUpTo = OnboardingGraph.OnboardingWelcomeRoute::class,
        inclusive = true,
    )
    data object LoginRoute : AuthGraph

    /**
     * Confirming the address finishes the funnel the same way [LoginRoute] does. Cancelling is the
     * implicit single-step `back()` Gezgin generates for every route that is not `@NoBack`, so no
     * annotation is needed for it.
     */
    @ReplaceTo(
        target = MainGraph.HomeRoute::class,
        clearUpTo = OnboardingGraph.OnboardingWelcomeRoute::class,
        inclusive = true,
    )
    data object LocationSelectionRoute : AuthGraph
}

@NavGraph
sealed interface MainGraph : Route {

    /**
     * Terminal for now: the signed-in area declares no outgoing edges yet, so `HomeNavigator`
     * carries only the implicit `back()`. Since Home is the stack root once the funnel is cleared,
     * that back reaches `onRootBack` and finishes the Activity.
     */
    data object HomeRoute : MainGraph
}
