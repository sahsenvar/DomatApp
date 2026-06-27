package com.domatapp.feature.auth.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.domatapp.feature.auth.data.datasource.AuthConfigDataSource
import com.domatapp.feature.auth.data.datasource.AuthConfigDataSourceImpl
import com.domatapp.feature.auth.data.datasource.AuthRemoteDataSource
import com.domatapp.feature.auth.data.datasource.AuthRemoteDataSourceImpl
import com.domatapp.feature.auth.data.datasource.UserProfileRemoteDataSource
import com.domatapp.feature.auth.data.datasource.UserProfileRemoteDataSourceImpl
import com.domatapp.feature.auth.data.repository.AuthRepositoryImpl
import com.domatapp.feature.auth.data.repository.UserProfileRepositoryImpl
import com.domatapp.feature.auth.domain.di.AuthDomainModule
import com.domatapp.feature.auth.domain.repository.AuthRepository
import com.domatapp.feature.auth.domain.repository.UserProfileRepository
import io.ktor.client.HttpClient
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module(includes = [AuthDomainModule::class])
class AuthDataModule {

    @Factory
    fun provideAuthRemoteDataSource(
        httpClient: HttpClient
    ): AuthRemoteDataSource = AuthRemoteDataSourceImpl(httpClient = httpClient)

    @Factory
    fun provideUserProfileRemoteDataSource(
        httpClient: HttpClient
    ): UserProfileRemoteDataSource = UserProfileRemoteDataSourceImpl(httpClient = httpClient)

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

    @Single
    fun provideUserProfileRepository(
        remoteSource: UserProfileRemoteDataSource
    ): UserProfileRepository = UserProfileRepositoryImpl(
        remoteSource = remoteSource
    )
}
