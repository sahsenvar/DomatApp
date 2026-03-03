package com.domatapp.feature.auth.presentation.viewmodel

import com.domatapp.core.common.presentation.BaseViewModel
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
 * Uses Moko MVVM's automatic viewModelScope and lifecycle management.
 */
@Factory
class AuthViewModel(
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
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
        viewModelScope.launch {
            emitEffect(AuthEffect.LaunchGoogleSignIn)
        }
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
        is AuthError.InvalidCredentials -> "Giriş bilgileri geçersiz"
        is AuthError.UserNotFound -> "Kullanıcı bulunamadı"
        is AuthError.EmailAlreadyInUse -> "Bu e-posta adresi zaten kullanımda"
        is AuthError.AccountDisabled -> "Hesabınız devre dışı bırakılmış"
        is RemoteError.NoConnection -> "İnternet bağlantısı yok"
        is RemoteError.Timeout -> "İstek zaman aşımına uğradı"
        is RemoteError.ServerError -> "Sunucu hatası (${this.code})"
        is RemoteError.ClientError -> "İstek hatası (${this.code})"
        else -> message ?: "Bilinmeyen bir hata oluştu"
    }
}
