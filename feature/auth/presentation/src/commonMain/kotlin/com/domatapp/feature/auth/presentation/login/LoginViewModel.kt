package com.domatapp.feature.auth.presentation.login

import androidx.lifecycle.viewModelScope
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationViewModel
import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.core.resource.api.StringResourceApi
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.error_account_disabled
import com.domatapp.core.resource.generated.resources.error_client
import com.domatapp.core.resource.generated.resources.error_email_already_in_use
import com.domatapp.core.resource.generated.resources.error_invalid_credentials
import com.domatapp.core.resource.generated.resources.error_no_connection
import com.domatapp.core.resource.generated.resources.error_server
import com.domatapp.core.resource.generated.resources.error_timeout
import com.domatapp.core.resource.generated.resources.error_unknown
import com.domatapp.core.resource.generated.resources.error_user_not_found
import com.domatapp.core.resulting.error.DomainError
import com.domatapp.core.resulting.error.RemoteError
import com.domatapp.feature.auth.domain.error.AuthError
import com.domatapp.feature.auth.domain.usecase.LoginWithGoogleUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.koin.core.annotation.KoinViewModel

/**
 * ViewModel for the Login screen.
 * MVI + exception-based error handling: the native UI owns the Google dialog,
 * this ViewModel owns the token exchange and the post-login routing decision.
 */
@NavigationViewModel(Route.AuthRoute.Login::class)
@KoinViewModel
class LoginViewModel(
    private val loginWithGoogle: LoginWithGoogleUseCase,
    private val stringResource: StringResourceApi
) : BaseViewModel<LoginUiState, LoginIntent, LoginEffect>(LoginUiState()) {

    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.OnGoogleSignInClicked -> {
                updateState { copy(isLoading = true) }
                emitEffect(LoginEffect.LaunchGoogleSignIn)
            }

            // todo: Bu işlemler handleResult'tan yönetilecek ileri de...
            is LoginIntent.OnGoogleTokenReceived -> loginWithGoogle(intent.idToken)
                .onStart {
                    updateState { copy(isLoading = true) }
                }
                .catch { exception ->
                    updateState { copy(isLoading = false) }
                    val domainError = exception as DomainError
                    val errorMessage = domainError.toUiMessage()
                    emitEffect(LoginEffect.ShowError(errorMessage))
                }
                .onEach { result ->
                    updateState { copy(isLoading = false) }
                    emitEffect(
                        if (result.hasUserExist) LoginEffect.NavigateToHome
                        else LoginEffect.NavigateToLocationSelection
                    )
                }.launchIn(viewModelScope) // todo: Burada DomatScope olacak

            is LoginIntent.OnGoogleSignInCancelled -> updateState {
                copy(isLoading = false)
            }
        }
    }

    /**
     * Convert DomainError to user-friendly UI message.
     */
    private suspend fun DomainError.toUiMessage(): String = when (this) {
        is AuthError.InvalidCredentials -> stringResource.getString(Res.string.error_invalid_credentials)
        is AuthError.UserNotFound -> stringResource.getString(Res.string.error_user_not_found)
        is AuthError.EmailAlreadyInUse -> stringResource.getString(Res.string.error_email_already_in_use)
        is AuthError.AccountDisabled -> stringResource.getString(Res.string.error_account_disabled)
        is RemoteError.NoConnection -> stringResource.getString(Res.string.error_no_connection)
        is RemoteError.Timeout -> stringResource.getString(Res.string.error_timeout)
        is RemoteError.ServerError -> stringResource.getString(Res.string.error_server, code)
        is RemoteError.ClientError -> stringResource.getString(Res.string.error_client, code)
        else -> message ?: stringResource.getString(Res.string.error_unknown)
    }
}
