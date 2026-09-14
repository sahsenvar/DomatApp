package com.domatapp.feature.auth.presentation.screen.login

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.domatapp.core.navigation.AuthGraph
import com.domatapp.core.navigation.LoginNavigator
import com.domatapp.core.presentation.screen.DomatEffectScope
import com.domatapp.core.presentation.screen.Effects
import com.domatapp.core.presentation.screen.ViewModelOf
import com.domatapp.feature.auth.presentation.login.LoginEffect
import com.domatapp.feature.auth.presentation.login.LoginIntent
import com.domatapp.feature.auth.presentation.login.LoginViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private const val WEB_CLIENT_ID =
    "60308278582-09rmm39o0mmpc5krfdjhp7kkd514j20e.apps.googleusercontent.com"

/** Fills `DomatScreenRoot`'s ViewModel slot for [AuthGraph.LoginRoute]. */
@ViewModelOf(AuthGraph.LoginRoute::class)
@Composable
fun loginViewModel(): LoginViewModel = koinViewModel()

/**
 * Fills `DomatScreenRoot`'s effect slot for [AuthGraph.LoginRoute].
 *
 * `nav` is a role parameter Gezgin supplies; [LoginNavigator] carries exactly the two edges
 * `AuthGraph.LoginRoute` declares, so `nav.goToLocationSelection()` and `nav.replaceToHome()` are
 * the only destinations reachable from here and anything else fails to compile.
 */
@Effects(AuthGraph.LoginRoute::class)
fun handleLoginEffect(
    effect: LoginEffect,
    scope: DomatEffectScope,
    onIntent: (LoginIntent) -> Unit,
    nav: LoginNavigator,
) {
    when (effect) {
        LoginEffect.LaunchGoogleSignIn ->
            scope.coroutineScope.launch { launchGoogleSignIn(scope.context, onIntent) }

        LoginEffect.NavigateToLocationSelection -> nav.goToLocationSelection()
        LoginEffect.NavigateToHome -> nav.replaceToHome()
        is LoginEffect.ShowError -> scope.showMessage(effect.message)
    }
}

private suspend fun launchGoogleSignIn(
    context: Context,
    onIntent: (LoginIntent) -> Unit,
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
