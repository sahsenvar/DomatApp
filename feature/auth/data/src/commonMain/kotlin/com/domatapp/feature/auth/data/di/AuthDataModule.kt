package com.domatapp.feature.auth.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.domatapp.feature.auth.data.datasource.AuthConfigSource
import com.domatapp.feature.auth.data.datasource.AuthConfigSourceImpl
import com.domatapp.feature.auth.data.datasource.AuthRemoteSource
import com.domatapp.feature.auth.data.datasource.UserProfileRemoteSource
import com.domatapp.feature.auth.data.datasource.impls.authRemoteSource
import com.domatapp.feature.auth.data.datasource.impls.userProfileRemoteSource
import com.domatapp.feature.auth.data.repository.AuthRepositoryImpl
import com.domatapp.feature.auth.data.repository.UserProfileRepositoryImpl
import com.domatapp.feature.auth.domain.di.AuthDomainModule
import com.domatapp.feature.auth.domain.repository.AuthRepository
import com.domatapp.feature.auth.domain.repository.UserProfileRepository
import cn.ktorfitx.multiplatform.core.Ktorfitx
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.module.Module as KoinModule
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module(includes = [AuthDomainModule::class])
class AuthDataModule {

    @Factory
    fun provideAuthRemoteSource(
        ktorfitx: Ktorfitx
    ): AuthRemoteSource = ktorfitx.authRemoteSource

    @Factory
    fun provideUserProfileRemoteSource(
        ktorfitx: Ktorfitx
    ): UserProfileRemoteSource = ktorfitx.userProfileRemoteSource

    @Single
    fun provideAuthConfigSource(
        @Named("auth") dataStore: DataStore<Preferences>
    ): AuthConfigSource = AuthConfigSourceImpl(dataStore = dataStore)

    @Single
    fun provideAuthRepository(
        remoteSource: AuthRemoteSource,
        configSource: AuthConfigSource
    ): AuthRepository = AuthRepositoryImpl(
        remoteSource = remoteSource,
        configSource = configSource
    )

    @Single
    fun provideUserProfileRepository(
        remoteSource: UserProfileRemoteSource
    ): UserProfileRepository = UserProfileRepositoryImpl(
        remoteSource = remoteSource
    )
}

/**
 * Entry point for loading this Koin module from another Gradle module.
 *
 * The Koin compiler plugin only generates the `module()` accessor inside the compilation that
 * declares the `@Module` class, so `AuthDataModule().module()` does not resolve from `:shared`.
 * Every module exposes its own accessor instead.
 */
fun authDataModule(): KoinModule = AuthDataModule().module()
