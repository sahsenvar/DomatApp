package com.domatapp.feature.auth.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.domatapp.feature.auth.data.datasource.AuthConfigDataSource
import com.domatapp.feature.auth.data.datasource.AuthConfigDataSourceImpl
import com.domatapp.feature.auth.data.datasource.AuthRemoteDataSource
import com.domatapp.feature.auth.data.datasource.AuthRemoteDataSourceImpl
import com.domatapp.feature.auth.data.repository.AuthRepositoryImpl
import com.domatapp.feature.auth.domain.di.AuthDomainModule
import com.domatapp.feature.auth.domain.repository.AuthRepository
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module(includes = [AuthDomainModule::class])
class AuthDataModule {

    @Factory
    fun provideAuthRemoteDataSource(
        httpClient: HttpClient,
        json: Json
    ): AuthRemoteDataSource = AuthRemoteDataSourceImpl(httpClient = httpClient, json = json)

    @Single
    fun provideAuthConfigDataSource(
        @Named("auth") dataStore: DataStore<Preferences>
    ): AuthConfigDataSource = AuthConfigDataSourceImpl(dataStore = dataStore)

    @Single
    fun provideAuthRepository(
        remoteDataSource: AuthRemoteDataSource,
        configDataSource: AuthConfigDataSource
    ): AuthRepository = AuthRepositoryImpl(
        remoteDataSource = remoteDataSource,
        configDataSource = configDataSource
    )
}
