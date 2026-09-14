package com.domatapp.feature.auth.data.repository

import com.domatapp.core.common.presentation.Environment
import com.domatapp.feature.auth.data.datasource.AuthConfigDataSource
import com.domatapp.feature.auth.data.datasource.AuthRemoteDataSource
import com.domatapp.feature.auth.data.mapper.toAuthError
import com.domatapp.feature.auth.data.remote.GoogleSignInRemoteModel
import com.domatapp.feature.auth.data.remote.toAuthSessionDomainModel
import com.domatapp.feature.auth.domain.model.AuthSessionDomainModel
import com.domatapp.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val configDataSource: AuthConfigDataSource
) : AuthRepository {

    override fun loginWithGoogle(idToken: String): Flow<AuthSessionDomainModel> = flow {
        val response = remoteDataSource.signInWithIdToken(
            grantType = "id_token",
            body = GoogleSignInRemoteModel(idToken = idToken)
        )

        // Update global accessToken for subsequent Ktor requests
        Environment.accessToken = response.accessToken

        // Persist token to DataStore
        configDataSource.saveToken(response.accessToken)

        emit(response.toAuthSessionDomainModel())
    }.catch { throw it.toAuthError() }

    override fun logout(): Flow<Unit> = flow {
        remoteDataSource.logout()
        Environment.accessToken = null
        configDataSource.clearAll()
        emit(Unit)
    }.catch { throw it.toAuthError() }
}
