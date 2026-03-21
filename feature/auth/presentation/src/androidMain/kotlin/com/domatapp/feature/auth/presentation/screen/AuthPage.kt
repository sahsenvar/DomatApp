package com.domatapp.feature.auth.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.domatapp.core.navigation.Route.AuthRoute
import com.domatapp.core.navigation.annotations.NavigationScreen
import androidx.compose.ui.res.colorResource
import com.domatapp.core.resource.R
import com.domatapp.feature.auth.presentation.model.AuthIntent
import com.domatapp.feature.auth.presentation.model.AuthUiState

@NavigationScreen(AuthRoute.AuthScreen::class)
@Composable
fun ColumnScope.AuthPage(
    uiState: AuthUiState,
    onIntent: (AuthIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.white)),
    ) {
        HeroSection()
        ContentSection(
            isLoading = uiState.isLoading,
            isSignInInProgress = uiState.isGoogleSignInInProgress,
            onGoogleSignInClick = { onIntent(AuthIntent.OnGoogleSignInClicked) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthPagePreview() = Column {
    AuthPage(
        uiState = AuthUiState(isLoading = false, isGoogleSignInInProgress = false),
        onIntent = {},
    )
}
