package com.domatapp.feature.auth.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.domatapp.core.navigation.Route
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.core.presentation.compose.LocalSnackbarHostState
import com.domatapp.feature.auth.presentation.model.AuthEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AuthEffectHandler(
    effectFlow: Flow<AuthEffect>,
) {
    val navigator = LocalNavigator.current
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                is AuthEffect.LaunchGoogleSignIn -> TODO()
                is AuthEffect.NavigateToHome -> navigator.replaceAll(Route.Main.Home)
                is AuthEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is AuthEffect.Idle -> Unit
            }
        }
    }
}
