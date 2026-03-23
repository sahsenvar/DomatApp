package com.domatapp.feature.auth.presentation.screen

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.domatapp.core.navigation.Route.AuthRoute
import com.domatapp.core.navigation.annotations.NavigationEffectHandler
import com.domatapp.core.presentation.compose.LocalSnackbarHostState
import com.domatapp.feature.auth.presentation.model.AuthEffect
import com.domatapp.feature.auth.presentation.model.AuthIntent
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

private const val WEB_CLIENT_ID =
    "60308278582-09rmm39o0mmpc5krfdjhp7kkd514j20e.apps.googleusercontent.com"

@NavigationEffectHandler(AuthRoute.AuthScreen::class)
@Composable
fun AuthEffectHandler(
    effectFlow: Flow<AuthEffect>,
    onIntent: (AuthIntent) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                is AuthEffect.LaunchGoogleSignIn -> launchGoogleSignIn(context, onIntent)
                is AuthEffect.NavigateToHome -> snackbarHostState.showSnackbar("Navigate to Home route when it's created")
                is AuthEffect.NavigateToAddressInput -> snackbarHostState.showSnackbar("Navigate to AddressInput route when it's created")
                is AuthEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }
}

private suspend fun launchGoogleSignIn(
    context: Context,
    onIntent: (AuthIntent) -> Unit
) = runCatching {
    val request = GetCredentialRequest.Builder().addCredentialOption(
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            //.setNonce() todo: Güvenlik için daha sonra eklenecek
            .build()
    ).build()

    val credential = CredentialManager.create(context)
        .getCredential(context, request)
        .credential

    val googleIdCredential = GoogleIdTokenCredential.createFrom(credential.data)

    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
        onIntent(AuthIntent.OnGoogleTokenReceived(googleIdCredential.idToken))
    else
        onIntent(AuthIntent.OnGoogleSignInCancelled)
}.onFailure {
    onIntent(AuthIntent.OnGoogleSignInCancelled)
}
