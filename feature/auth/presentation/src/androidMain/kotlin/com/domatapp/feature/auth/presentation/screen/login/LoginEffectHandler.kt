package com.domatapp.feature.auth.presentation.screen.login

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationEffectHandler
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.core.presentation.compose.LocalSnackbarHostState
import com.domatapp.feature.auth.presentation.login.LoginEffect
import com.domatapp.feature.auth.presentation.login.LoginIntent
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

private const val WEB_CLIENT_ID =
    "60308278582-09rmm39o0mmpc5krfdjhp7kkd514j20e.apps.googleusercontent.com"

@NavigationEffectHandler(Route.AuthRoute.Login::class)
@Composable
fun LoginEffectHandler(
    effectFlow: Flow<LoginEffect>,
    onIntent: (LoginIntent) -> Unit
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                LoginEffect.LaunchGoogleSignIn -> launchGoogleSignIn(context, onIntent)
                LoginEffect.NavigateToHome -> navigator.replaceAll(Route.Main.Home)
                LoginEffect.NavigateToLocationSelection ->
                    navigator.navigate(Route.AuthRoute.LocationSelection)

                is LoginEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }
}

private suspend fun launchGoogleSignIn(
    context: Context,
    onIntent: (LoginIntent) -> Unit
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
        onIntent(LoginIntent.OnGoogleTokenReceived(googleIdCredential.idToken))
    else
        onIntent(LoginIntent.OnGoogleSignInCancelled)
}.onFailure {
    onIntent(LoginIntent.OnGoogleSignInCancelled)
}
