package com.domatapp.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import dev.gezgin.core.compose.GezginTransition
import dev.gezgin.core.compose.navTransitions

private const val SLIDE_DURATION_MS = 300

/**
 * The app-level default transition, carried over unchanged from the `transitionSpec` /
 * `popTransitionSpec` arguments the old hand-wired `NavDisplay` call used.
 *
 * Gezgin resolves transitions innermost-first: a route's own `transition` override wins, then its
 * graph's, and this app-level value is the last resort. That cascade replaces the per-entry
 * `NavDisplay.transitionSpec` metadata the old `OnboardingEntries.kt` passed by hand - to restore
 * it, add to the route or its graph in `:core:navigation`:
 *
 * ```
 * override val transition get() = transition {
 *     forward { EnterTransition.None togetherWith ExitTransition.None }
 * }
 * ```
 *
 * It was not carried over because the route it applied to is the start destination: nothing
 * animates into it, and the auth funnel clears it rather than popping back to it.
 */
val DomatNavTransitions: GezginTransition = navTransitions {
    forward {
        slideInHorizontally(animationSpec = tween(SLIDE_DURATION_MS, easing = LinearEasing)) { it } togetherWith
                slideOutHorizontally(animationSpec = tween(SLIDE_DURATION_MS, easing = LinearEasing)) { -it }
    }
    backward {
        slideInHorizontally(animationSpec = tween(SLIDE_DURATION_MS, easing = LinearEasing)) { -it } togetherWith
                slideOutHorizontally(animationSpec = tween(SLIDE_DURATION_MS, easing = LinearEasing)) { it }
    }
}
