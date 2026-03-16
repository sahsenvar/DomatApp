package com.domatapp.feature.auth.data.datasource

import com.domatapp.core.config.annotations.ClearAllLocalConfig
import com.domatapp.core.config.annotations.ClearLocalConfig
import com.domatapp.core.config.annotations.ConfigDataSource
import com.domatapp.core.config.annotations.ObserveLocalConfig
import com.domatapp.core.config.annotations.RetrieveLocalConfig
import com.domatapp.core.config.annotations.SaveLocalConfig
import kotlinx.coroutines.flow.Flow

@ConfigDataSource(name = "auth")
interface AuthConfigDataSource {

    @SaveLocalConfig(key = "access_token")
    suspend fun saveToken(token: String)

    @RetrieveLocalConfig(key = "access_token")
    suspend fun retrieveToken(): String?

    @ObserveLocalConfig(key = "access_token")
    fun observeToken(): Flow<String?>

    @ClearLocalConfig(key = "access_token")
    suspend fun clearToken()

    @ClearAllLocalConfig
    suspend fun clearAll()
}
