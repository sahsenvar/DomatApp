package com.domatapp.feature.auth.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationViewModel
import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.core.resource.MR
import com.domatapp.core.resource.api.StringResourceApi
import com.domatapp.core.resulting.error.DomainError
import com.domatapp.core.resulting.error.RemoteError
import com.domatapp.feature.auth.domain.error.AuthError
import com.domatapp.feature.auth.domain.usecase.LoginWithGoogleUseCase
import com.domatapp.feature.auth.presentation.model.AuthEffect
import com.domatapp.feature.auth.presentation.model.AuthIntent
import com.domatapp.feature.auth.presentation.model.AuthUiState
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.koin.android.annotation.KoinViewModel

/**
 * ViewModel for Authentication screen.
 * Implements MVI pattern with exception-based error handling.
 */
@NavigationViewModel(Route.AuthRoute.AuthScreen::class)
@KoinViewModel
class AuthViewModel(
    private val loginWithGoogle: LoginWithGoogleUseCase,
    private val stringResource: StringResourceApi
) : BaseViewModel<AuthUiState, AuthIntent, AuthEffect>(
    initialState = AuthUiState()
) {

    override fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.OnGoogleSignInClicked -> {
                updateState { copy(isLoading = true) }
                emitEffect(AuthEffect.LaunchGoogleSignIn)
            }

            // todo: Bu işlemler handleResult'tan yönetilecek ileri de...
            is AuthIntent.OnGoogleTokenReceived -> loginWithGoogle(intent.idToken)
                .onStart {
                    updateState { copy(isLoading = true) }
                }
                .catch { exception ->
                    updateState { copy(isLoading = false) }
                    val domainError = exception as DomainError
                    val errorMessage = domainError.toUiMessage()
                    emitEffect(AuthEffect.ShowError(errorMessage))
                }
                .onEach { result ->
                    updateState { copy(isLoading = false) }
                    emitEffect(if (result.hasUserExist) AuthEffect.NavigateToHome else AuthEffect.NavigateToAddressInput)
                }.launchIn(viewModelScope) // todo: Burada DomatScope olacak

            is AuthIntent.OnGoogleSignInCancelled -> updateState {
                copy(isLoading = false)
            }
        }
    }

    /**
     * Convert DomainError to user-friendly UI message.
     */
    private fun DomainError.toUiMessage(): String = when (this) {
        is AuthError.InvalidCredentials -> stringResource.getString(MR.strings.error_invalid_credentials)
        is AuthError.UserNotFound -> stringResource.getString(MR.strings.error_user_not_found)
        is AuthError.EmailAlreadyInUse -> stringResource.getString(MR.strings.error_email_already_in_use)
        is AuthError.AccountDisabled -> stringResource.getString(MR.strings.error_account_disabled)
        is RemoteError.NoConnection -> stringResource.getString(MR.strings.error_no_connection)
        is RemoteError.Timeout -> stringResource.getString(MR.strings.error_timeout)
        is RemoteError.ServerError -> stringResource.getString(MR.strings.error_server, code)
        is RemoteError.ClientError -> stringResource.getString(MR.strings.error_client, code)
        else -> message ?: stringResource.getString(MR.strings.error_unknown)
    }
}
