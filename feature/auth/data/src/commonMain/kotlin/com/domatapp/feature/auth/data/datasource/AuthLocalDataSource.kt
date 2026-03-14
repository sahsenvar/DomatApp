package com.domatapp.feature.auth.data.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.domatapp.core.local.annotations.ClearAll
import kotlinx.coroutines.flow.Flow

/**
 * Local data source for authentication.
 */
@Dao
@DataS
interface AuthLocalDataSource {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveToken(token: String)
    fun retrieveToken(): Flow<String?>


    suspend fun clearToken()

    @ClearAll()
    suspend fun clearAll()
}