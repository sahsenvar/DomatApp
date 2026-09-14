package com.domatapp.core.presentation.screen

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.core.presentation.compose.LocalSnackbarHostState
import dev.gezgin.core.Route
import dev.gezgin.core.annotation.FilledBy
import dev.gezgin.core.annotation.Screen
import dev.gezgin.core.annotation.ScreenSlot
import dev.gezgin.core.annotation.ScreenWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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

/**
 * Everything an [Effects] handler needs that it cannot obtain itself, because it is a plain
 * function outside composition.
 *
 * Deliberately **not** generic. Gezgin's slot unifier does no generic decomposition except on
 * function types, so a slot parameter of type `DomatEffectScope<I>` would be compared as an opaque
 * concrete type against the provider's `DomatEffectScope<LoginIntent>` and fail to match (`SW8`).
 * The typed intent sink is therefore its own `(I) -> Unit` slot parameter, which the unifier does
 * decompose - and which is what binds `I` for routes whose content slot did not already.
 *
 * @property context the host Activity context - Credential Manager and other OS-modal APIs need it.
 * @property coroutineScope a composition-scoped [CoroutineScope] for suspending work started here.
 * @property showMessage shows a snackbar on the app-level host; safe to call from a plain function.
 */
class DomatEffectScope(
    val context: Context,
    val coroutineScope: CoroutineScope,
    val showMessage: (String) -> Unit,
)

/**
 * The application's single screen root - the one place that decides what a screen *is*.
 *
 * It owns the container, resolves the ViewModel, collects state, and sets the side-effect policy.
 * Gezgin fills the slots: for each route it finds the one `@ViewModelOf`, the optional `@Effects`
 * and the `@Screen` composable, checks their signatures against the slots below, and generates a
 * `provideXEntry()` that calls this function with them. This replaces the per-screen
 * `{Name}Route.kt` files the removed in-repo `NavigationProcessor` used to write.
 *
 * **No slot here has a Kotlin default, and that is deliberate.** A wrapper type parameter must be
 * bound by some *filled* slot or the processor rejects the binding (`SW7`), and the slot's return
 * type is not part of unification - so `E` is bound only by `onEffect`, never by `viewModel`'s
 * `BaseViewModel<S, I, E>` return type. Defaulting `onEffect` would therefore break exactly the
 * screens that have no effects. Every route declares all three providers.
 *
 * A route whose `@Screen` signature does not match `ColumnScope.(S, (I) -> Unit)` falls back to an
 * *unwrapped* entry with a `[SW6]` KSP warning rather than an error - worth watching for in build
 * output.
 *
 * Declared in `:core:presentation`, which every feature presentation module depends on but none of
 * them is compiled together with. KSP cannot enumerate annotated declarations on the classpath, so
 * each of those modules names this package through the `gezgin.wrapperPackages` KSP argument. A
 * feature that forgets it gets unwrapped entries, not a build failure.
 */
@ScreenWrapper
@Composable
fun <S : Any, I : Any, E : Any> DomatScreenRoot(
    @FilledBy(ViewModelOf::class) viewModel: @Composable () -> BaseViewModel<S, I, E>,
    @FilledBy(Effects::class) onEffect: (E, DomatEffectScope, (I) -> Unit) -> Unit,
    @FilledBy(Screen::class) content: @Composable ColumnScope.(S, (I) -> Unit) -> Unit,
) {
    val vm = viewModel()
    val uiState by vm.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()

    val effectScope = remember(context, snackbarHostState, coroutineScope) {
        DomatEffectScope(
            context = context,
            coroutineScope = coroutineScope,
            showMessage = { message ->
                coroutineScope.launch { snackbarHostState.showSnackbar(message) }
            },
        )
    }

    // BaseViewModel.effect is Channel-backed, so an effect emitted while this entry is off-screen
    // is held rather than dropped. A covered Navigation 3 entry leaves composition entirely, which
    // is exactly the window a replay = 0 SharedFlow would lose.
    LaunchedEffect(vm) {
        vm.effect.collect { effect -> onEffect(effect, effectScope, vm::onIntent) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        content(uiState, vm::onIntent)
    }
}
