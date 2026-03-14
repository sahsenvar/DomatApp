package com.domatapp.feature.auth.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.domatapp.feature.auth.data.datasource.AuthLocalDataSource
import com.domatapp.feature.auth.data.datasource.AuthLocalDataSourceImpl
import com.domatapp.feature.auth.data.datasource.AuthRemoteDataSource
import com.domatapp.feature.auth.data.datasource.AuthRemoteDataSourceImpl
import io.ktor.client.HttpClient
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.domatapp.feature.auth.data")
class AuthDataModule {

    @OptIn(InternalSerializationApi::class)
    @Single
    fun provideAuthRemoteDataSource(
        httpClient: HttpClient,
        json: Json
    ): AuthRemoteDataSource = AuthRemoteDataSourceImpl(httpClient, json)

    @Single
    fun provideAuthLocalDataSource(
        @Named("authDataStore") dataStore: DataStore<Preferences>
    ): AuthLocalDataSource = AuthLocalDataSourceImpl(dataStore)
}

