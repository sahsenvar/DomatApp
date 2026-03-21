package com.domatapp.feature.auth.presentation.login

import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationViewModel
import com.domatapp.core.presentation.base.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@NavigationViewModel(Route.AuthRoute.Login::class)
@KoinViewModel
class LoginViewModel : BaseViewModel<
    LoginUiState,
    LoginIntent,
    LoginEffect
>(LoginUiState()) {
    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            LoginIntent.OnGoogleSignInClicked ->
                emitEffect(LoginEffect.NavigateToLocationSelection)
        }
    }
}
