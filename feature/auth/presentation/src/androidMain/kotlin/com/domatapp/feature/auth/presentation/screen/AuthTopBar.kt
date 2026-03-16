package com.domatapp.feature.auth.presentation.screen

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.domatapp.core.navigation.Route.AuthRoute
import com.domatapp.core.navigation.annotations.TopBar
import com.domatapp.feature.auth.presentation.model.AuthIntent
import com.domatapp.feature.auth.presentation.model.AuthUiState

@TopBar(AuthRoute.AuthScreen::class)
@Composable
fun AuthTopBar(
    uiState: AuthUiState,
    onIntent: (AuthIntent) -> Unit
) {
    @OptIn(ExperimentalMaterial3Api::class)
    CenterAlignedTopAppBar(
        title = { Text(text = "Auth") }
    )
}