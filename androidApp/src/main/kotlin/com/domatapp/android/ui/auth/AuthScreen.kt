package com.domatapp.android.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.domatapp.feature.auth.presentation.model.AuthEffect
import com.domatapp.feature.auth.presentation.model.AuthIntent
import com.domatapp.feature.auth.presentation.viewmodel.AuthViewModel
import org.koin.compose.koinInject

/**
 * Auth screen for Android.
 * Uses shared AuthViewModel from presentation layer.
 */
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = koinInject(),
    onGoogleSignInRequested: () -> String,
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.LaunchGoogleSignIn -> {
                    val idToken = onGoogleSignInRequested()
                    viewModel.onIntent(AuthIntent.OnGoogleTokenReceived(idToken))
                }
                is AuthEffect.NavigateToHome -> onNavigateToHome()
                is AuthEffect.ShowError -> {
                    // TODO: Show error snackbar
                    println("Error: ${effect.message}")
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to DomatApp",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                uiState.session != null -> {
                    Text("Authenticated as: ${uiState.session?.email}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { /* TODO: Logout */ }) {
                        Text("Logout")
                    }
                }

                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.onIntent(AuthIntent.OnGoogleSignInClicked) }
                    ) {
                        Text("Retry")
                    }
                }

                else -> {
                    Button(
                        onClick = { viewModel.onIntent(AuthIntent.OnGoogleSignInClicked) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isGoogleSignInInProgress
                    ) {
                        if (uiState.isGoogleSignInInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Sign in with Google")
                        }
                    }
                }
            }
        }
    }
}
