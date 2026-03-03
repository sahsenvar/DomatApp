package com.domatapp.app.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.domatapp.feature.auth.presentation.model.AuthEffect
import com.domatapp.feature.auth.presentation.model.AuthIntent
import com.domatapp.feature.auth.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

/**
 * Google Sign-In test screen.
 * This is a simple test UI for authentication flow.
 */
@Composable
fun AuthScreen(
    onGoogleSignInRequested: suspend () -> String?, // Returns idToken or null
    onNavigateToHome: () -> Unit = {},
    viewModel: AuthViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AuthEffect.LaunchGoogleSignIn -> {
                    // Launch native Google Sign-In
                    val idToken = onGoogleSignInRequested()
                    if (idToken != null) {
                        viewModel.onIntent(AuthIntent.OnGoogleTokenReceived(idToken))
                    } else {
                        viewModel.onIntent(AuthIntent.OnGoogleSignInCancelled)
                    }
                }
                is AuthEffect.NavigateToHome -> {
                    onNavigateToHome()
                }
                is AuthEffect.ShowError -> {
                    // Error already shown in UI via state
                }
            }
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "DomatApp",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Test Google Sign-In",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Loading indicator
                if (state.isLoading) {
                    CircularProgressIndicator()
                    Text(
                        text = "Giriş yapılıyor...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Google Sign-In Button
                if (!state.isLoading) {
                    Button(
                        onClick = {
                            viewModel.onIntent(AuthIntent.OnGoogleSignInClicked)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !state.isGoogleSignInInProgress
                    ) {
                        Text("Google ile Giriş Yap")
                    }
                }

                // Error message
                if (state.error != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Hata",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = state.error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            TextButton(
                                onClick = {
                                    viewModel.onIntent(AuthIntent.OnErrorDismissed)
                                }
                            ) {
                                Text("Kapat")
                            }
                        }
                    }
                }

                // Session info (for testing)
                if (state.session != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Giriş Başarılı!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "User ID: ${state.session!!.userId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Email: ${state.session!!.email}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Debug info
                Text(
                    text = "State: Loading=${state.isLoading}, " +
                            "SignInProgress=${state.isGoogleSignInInProgress}, " +
                            "HasSession=${state.session != null}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
