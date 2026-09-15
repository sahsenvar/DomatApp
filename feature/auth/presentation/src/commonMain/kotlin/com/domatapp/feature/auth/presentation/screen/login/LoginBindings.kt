package com.domatapp.feature.auth.presentation.screen.login

import androidx.compose.runtime.Composable
import com.domatapp.core.common.presentation.Environment
import com.domatapp.core.navigation.AuthGraph
import com.domatapp.core.navigation.LoginNavigator
import com.domatapp.core.presentation.screen.DomatEffectScope
import com.domatapp.core.presentation.screen.Effects
import com.domatapp.core.presentation.screen.ViewModelOf
import com.domatapp.feature.auth.presentation.login.LoginEffect
import com.domatapp.feature.auth.presentation.login.LoginIntent
import com.domatapp.feature.auth.presentation.login.LoginViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

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
 *
 * Common to both platforms since the screens moved out of `androidMain`. The one part that cannot
 * be - putting up the OS account picker - is [requestGoogleIdToken], whose `actual`s are Credential
 * Manager on Android and the Swift-side GoogleSignIn-iOS SDK on iOS. The MVI contract above it is
 * unchanged: the handler still turns the token (or the user's cancellation) back into an intent.
 */
@Effects(AuthGraph.LoginRoute::class)
fun handleLoginEffect(
    effect: LoginEffect,
    scope: DomatEffectScope,
    onIntent: (LoginIntent) -> Unit,
    nav: LoginNavigator,
) {
    when (effect) {
        LoginEffect.LaunchGoogleSignIn -> scope.coroutineScope.launch {
            val idToken = requestGoogleIdToken(
                context = scope.context,
                serverClientId = Environment.googleWebClientId,
            )
            onIntent(
                if (idToken == null) LoginIntent.OnGoogleSignInCancelled
                else LoginIntent.OnGoogleTokenReceived(idToken)
            )
        }

        LoginEffect.NavigateToLocationSelection -> nav.goToLocationSelection()
        LoginEffect.NavigateToHome -> nav.replaceToHome()
        is LoginEffect.ShowError -> scope.showMessage(effect.message)
    }
}
