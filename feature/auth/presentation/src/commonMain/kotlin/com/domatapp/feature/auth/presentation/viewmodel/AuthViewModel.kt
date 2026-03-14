package com.domatapp.feature.auth.presentation.viewmodel

import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

/**
 * ViewModel for Authentication screen.
 * Implements MVI pattern with exception-based error handling.
 * Uses Lifecycle ViewModel's viewModelScope and lifecycle management.
 */
@Factory
class AuthViewModel(
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val stringResource: StringResourceApi
) : BaseViewModel<AuthUiState, AuthIntent, AuthEffect>(
    initialState = AuthUiState()
) {

    override fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.OnGoogleSignInClicked -> handleGoogleSignInClicked()
            is AuthIntent.OnGoogleTokenReceived -> handleGoogleTokenReceived(intent.idToken)
            is AuthIntent.OnGoogleSignInCancelled -> handleGoogleSignInCancelled()
            is AuthIntent.OnErrorDismissed -> handleErrorDismissed()
        }
    }

    private fun handleGoogleSignInClicked() {
        updateState { it.copy(isGoogleSignInInProgress = true) }
            emitEffect(AuthEffect.LaunchGoogleSignIn)
    }

    private fun handleGoogleTokenReceived(idToken: String) {
        viewModelScope.launch {
            loginWithGoogleUseCase(idToken)
                .onStart {
                    updateState {
                        it.copy(
                            isLoading = true,
                            error = null,
                            isGoogleSignInInProgress = false
                        )
                    }
                }
                .catch { exception ->
                    // Map exception to domain error and handle
                    val domainError = exception as? DomainError
                        ?: RemoteError.Unknown(exception.message, exception)
                    val errorMessage = domainError.toUiMessage()

                    updateState {
                        it.copy(
                            isLoading = false,
                            error = errorMessage,
                            isGoogleSignInInProgress = false
                        )
                    }

                    emitEffect(AuthEffect.ShowError(errorMessage))
                }
                .collect { session ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            session = session,
                            error = null,
                            isGoogleSignInInProgress = false
                        )
                    }

                    // Navigate to home after successful login
                    emitEffect(AuthEffect.NavigateToHome)
                }
        }
    }

    private fun handleGoogleSignInCancelled() {
        updateState {
            it.copy(
                isLoading = false,
                isGoogleSignInInProgress = false,
                error = null
            )
        }
    }

    private fun handleErrorDismissed() {
        updateState { it.copy(error = null) }
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
