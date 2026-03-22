package com.domatapp.feature.auth.data.repository

import com.domatapp.feature.auth.data.datasource.AuthConfigDataSource
import com.domatapp.feature.auth.data.datasource.AuthRemoteDataSource
import com.domatapp.feature.auth.data.mapper.toAuthError
import com.domatapp.feature.auth.domain.model.AuthSession
import com.domatapp.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    //private val localDataSource: AuthLocalDataSource,
    private val configDataSource: AuthConfigDataSource
) : AuthRepository {

    override fun loginWithGoogle(idToken: String): Flow<AuthSession> = TODO()
    //    val dto = remoteDataSource.signInWithGoogle(GoogleSignInRequest(idToken))
//
    //    // TODO: Save to local data source
    //    // configDataSource.saveToken("sample")
    //    // localDataSource.saveSession(...)
//
    //    emit(dto.toAuthSession())
    //}.retryWhen { cause, attempt ->
    //    if (cause is RemoteError.NoConnection && attempt < 3) {
    //        delay(1000 * (attempt + 1))
    //        true
    //    } else {
    //        false
    //    }
    //}.catch { throw it.toAuthError() }

    override fun observeAuthSession(): Flow<AuthSession?> = flow {
        // TODO: Observe from config data source
        // configDataSource.retrieveToken() ...
        emit(null)
    }.catch { throw it.toAuthError() }

    override fun logout(): Flow<Unit> = flow {
        val token = "dummy-token"

        remoteDataSource.logout(token)

        configDataSource.clearAll()
        //localDataSource.deleteAll()

        emit(Unit)
    }.catch { throw it.toAuthError() }
}
