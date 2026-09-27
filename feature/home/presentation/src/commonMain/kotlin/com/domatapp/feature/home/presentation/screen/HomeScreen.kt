package com.domatapp.feature.home.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.domatapp.core.navigation.HomeNavigator
import com.domatapp.core.navigation.MainGraph
import com.domatapp.core.presentation.screen.DomatEffectScope
import com.domatapp.core.presentation.screen.Effects
import com.domatapp.core.presentation.screen.ViewModelOf
import com.domatapp.feature.home.presentation.home.HomeEffect
import com.domatapp.feature.home.presentation.home.HomeIntent
import com.domatapp.feature.home.presentation.home.HomeUiState
import com.domatapp.feature.home.presentation.home.HomeViewModel
import dev.gezgin.core.annotation.Screen

/**
 * The ViewModel has no injected collaborators, so this provider goes straight to `viewModel { }`
 * rather than Koin - Gezgin does not care which one an app uses.
 */
@ViewModelOf(MainGraph.HomeRoute::class)
@Composable
fun homeViewModel(): HomeViewModel = viewModel { HomeViewModel() }

/**
 * Home emits no effects yet, but the provider is still required: `DomatScreenRoot`'s effect slot
 * has no Kotlin default, because it is the only slot that binds the wrapper's `E` type parameter
 * (a slot's return type is not unified, so `viewModel`'s `BaseViewModel<S, I, E>` does not bind it).
 * An unfilled slot would leave `E` unbound and fail with `[SW7]`.
 *
 * `HomeEffect` has no members, so there is nothing to branch on.
 */
@Effects(MainGraph.HomeRoute::class)
fun handleHomeEffect(
    effect: HomeEffect,
    scope: DomatEffectScope,
    onIntent: (HomeIntent) -> Unit,
    nav: HomeNavigator,
) = Unit

@Screen(MainGraph.HomeRoute::class)
@Composable
fun ColumnScope.HomeScreen(
    uiState: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Welcome Home",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    androidx.compose.foundation.layout.Column {
        HomeScreen(uiState = HomeUiState(), onIntent = {})
    }
}
