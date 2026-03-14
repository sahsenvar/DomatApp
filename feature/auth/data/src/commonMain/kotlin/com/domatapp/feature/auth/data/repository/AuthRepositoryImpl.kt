package com.domatapp.feature.auth.data.repository

import com.domatapp.core.local.database.dao.AuthSessionDao
import com.domatapp.core.resulting.error.RemoteError
import com.domatapp.feature.auth.data.datasource.AuthLocalDataSource
import com.domatapp.feature.auth.data.datasource.AuthRemoteDataSource
import com.domatapp.feature.auth.data.dto.GoogleSignInRequest
import com.domatapp.feature.auth.data.mapper.toAuthError
import com.domatapp.feature.auth.data.mapper.toDomain
import com.domatapp.feature.auth.domain.model.AuthSession
import com.domatapp.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import org.koin.core.annotation.Single

/**
 * Implementation of AuthRepository using exception-based error handling.
 */
@Single
class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val localDataSource: AuthLocalDataSource,
    private val authSessionDao: AuthSessionDao
) : AuthRepository {

    override fun loginWithGoogle(idToken: String): Flow<AuthSession> = flow {
        val dto = remoteDataSource.signInWithGoogle(GoogleSignInRequest(idToken))

        // TODO: Save to local data source
        // localDataSource.saveToken("sample")
        // authSessionDao.insert(...)

        emit(dto.toDomain())
    }.retryWhen { cause, attempt ->
        // Retry only on remote connection errors, max 3 attempts
        if (cause is RemoteError.NoConnection && attempt < 3) {
            delay(1000 * (attempt + 1)) // Exponential backoff
            true
        } else {
            false
        }
    }.catch { throw it.toAuthError() }

    override fun observeAuthSession(): Flow<AuthSession?> = flow {
        // TODO: Observe from local data source
        // localDataSource.retrieveToken() ...
        emit(null)
    }.catch { throw it.toAuthError() }

    override fun logout(): Flow<Unit> = flow {
        // TODO: Retrieve current token
        val token = "dummy-token"

        // Call remote logout
        remoteDataSource.logout(token)

        // Clear local session
        localDataSource.clearAll()
        authSessionDao.deleteAll()

        emit(Unit)
    }.catch { throw it.toAuthError() }
}
