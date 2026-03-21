package com.domatapp.feature.auth.presentation.screen.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationEffectHandler
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.feature.auth.presentation.location.LocationSelectionEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@NavigationEffectHandler(Route.AuthRoute.LocationSelection::class)
@Composable
fun LocationSelectionEffectHandler(effectFlow: Flow<LocationSelectionEffect>) {
    val navigator = LocalNavigator.current
    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                LocationSelectionEffect.NavigateToHome -> navigator.replaceAll(Route.Main.Home)
                LocationSelectionEffect.NavigateBack -> navigator.popBack()
            }
        }
    }
}
