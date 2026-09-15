package com.domatapp.core.presentation.screen

import dev.gezgin.core.Route
import dev.gezgin.core.annotation.ScreenSlot
import kotlin.reflect.KClass

/**
 * Binds a `@Composable` provider to the ViewModel of [route].
 *
 * The provider calls DI itself (`koinViewModel()` here, or a plain `viewModel { }` for a screen
 * that has no injected collaborators) and may take the route instance as a parameter when it needs
 * its arguments. Gezgin never resolves a ViewModel - it only supplies the typed route.
 */
@ScreenSlot
@Repeatable
annotation class ViewModelOf(val route: KClass<out Route>)

/**
 * Binds a **plain** (non-composable) effect handler to [route].
 *
 * The handler takes the effect, a [DomatEffectScope] for the capabilities a plain function cannot
 * reach on its own (an Android `Context`, a `CoroutineScope`, the app snackbar), the owning
 * ViewModel's typed intent sink, and - supplied by Gezgin as a *role* parameter, not a slot one -
 * that route's navigator, written by its exact simple name (`LoginNavigator`, ...).
 *
 * [DomatScreenRoot] is what collects `BaseViewModel.effect` and calls this; the handler itself is
 * a `when` over the effect type and nothing else.
 */
@ScreenSlot
@Repeatable
annotation class Effects(val route: KClass<out Route>)
