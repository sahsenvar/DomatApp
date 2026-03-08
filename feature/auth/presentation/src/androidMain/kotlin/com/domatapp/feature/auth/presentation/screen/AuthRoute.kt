package com.domatapp.feature.auth.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.domatapp.feature.auth.presentation.viewmodel.AuthViewModel
import org.koin.compose.koinInject

@Composable
fun AuthRoute(
    viewModel: AuthViewModel = koinInject()
) {
    val uiState by viewModel.state.collectAsState()
    AuthEffectHandler(effectFlow = viewModel.effect)

    AuthScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent
    )
}
