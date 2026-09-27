package com.domatapp.feature.auth.presentation.screen.location

import androidx.compose.runtime.Composable
import com.domatapp.core.navigation.AuthGraph
import com.domatapp.core.navigation.LocationSelectionNavigator
import com.domatapp.core.presentation.screen.DomatEffectScope
import com.domatapp.core.presentation.screen.Effects
import com.domatapp.core.presentation.screen.ViewModelOf
import com.domatapp.feature.auth.presentation.location.LocationSelectionEffect
import com.domatapp.feature.auth.presentation.location.LocationSelectionIntent
import com.domatapp.feature.auth.presentation.location.LocationSelectionViewModel
import org.koin.compose.viewmodel.koinViewModel

/** Fills `DomatScreenRoot`'s ViewModel slot for [AuthGraph.LocationSelectionRoute]. */
@ViewModelOf(AuthGraph.LocationSelectionRoute::class)
@Composable
fun locationSelectionViewModel(): LocationSelectionViewModel = koinViewModel()

/**
 * `back()` is the implicit single-step edge Gezgin generates for every route that is not
 * `@NoBack`; `replaceToHome()` comes from this route's `@ReplaceTo`.
 *
 * `scope` and `onIntent` are unused here but stay in the signature: the slot type is
 * `(E, DomatEffectScope, (I) -> Unit) -> Unit`, and a handler whose parameter list does not match
 * it exactly fails to bind (`SW8`).
 */
@Effects(AuthGraph.LocationSelectionRoute::class)
fun handleLocationSelectionEffect(
    effect: LocationSelectionEffect,
    scope: DomatEffectScope,
    onIntent: (LocationSelectionIntent) -> Unit,
    nav: LocationSelectionNavigator,
) {
    when (effect) {
        LocationSelectionEffect.NavigateToHome -> nav.replaceToHome()
        LocationSelectionEffect.NavigateBack -> nav.back()
    }
}
