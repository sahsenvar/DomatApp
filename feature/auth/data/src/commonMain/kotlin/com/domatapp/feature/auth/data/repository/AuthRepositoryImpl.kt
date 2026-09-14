package com.domatapp.feature.auth.data.repository

import com.domatapp.core.common.presentation.Environment
import com.domatapp.feature.auth.data.datasource.AuthConfigSource
import com.domatapp.feature.auth.data.datasource.AuthRemoteSource
import com.domatapp.feature.auth.data.mapper.toAuthError
import com.domatapp.feature.auth.data.remote.GoogleSignInRemoteModel
import com.domatapp.feature.auth.data.remote.toAuthSessionDomainModelResult
import com.domatapp.feature.auth.domain.model.AuthSessionDomainModel
import com.domatapp.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
class AuthRepositoryImpl(
    private val remoteSource: AuthRemoteSource,
    private val configSource: AuthConfigSource
) : AuthRepository {

    override fun loginWithGoogle(idToken: String): Flow<AuthSessionDomainModel> = flow {
        val response = remoteSource.signInWithIdToken(
            grantType = "id_token",
            body = GoogleSignInRemoteModel(idToken = idToken)
        )

        // Update global accessToken for subsequent Ktor requests
        Environment.accessToken = response.accessToken

        // Persist token to DataStore
        configSource.saveToken(response.accessToken)

        emit(response.toAuthSessionDomainModelResult().getOrThrow())
    }.catch { throw it.toAuthError() }

    override fun logout(): Flow<Unit> = flow {
        remoteSource.logout()
        Environment.accessToken = null
        configSource.clearAll()
        emit(Unit)
    }.catch { throw it.toAuthError() }
}
