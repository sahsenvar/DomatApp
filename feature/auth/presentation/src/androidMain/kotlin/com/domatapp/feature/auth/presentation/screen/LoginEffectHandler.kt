package com.domatapp.feature.auth.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationEffectHandler
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.feature.auth.presentation.model.login.LoginEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@NavigationEffectHandler(Route.AuthRoute.Login::class)
@Composable
fun LoginEffectHandler(effectFlow: Flow<LoginEffect>) {
    val navigator = LocalNavigator.current
    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                LoginEffect.NavigateToLocationSelection ->
                    navigator.navigate(Route.AuthRoute.LocationSelection)
            }
        }
    }
}
