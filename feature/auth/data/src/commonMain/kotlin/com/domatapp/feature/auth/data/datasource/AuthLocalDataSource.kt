package com.domatapp.feature.auth.data.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.domatapp.feature.auth.data.dto.AuthSessionEntity

@Dao
interface AuthLocalDataSource {
    @Query("SELECT * FROM auth_session WHERE id = :id")
    suspend fun getById(id: String): AuthSessionEntity?

    @Query("SELECT * FROM auth_session WHERE expiresAt > :currentTime ORDER BY createdAt DESC LIMIT 1")
    suspend fun getActive(currentTime: Long): AuthSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: AuthSessionEntity)

    @Query("DELETE FROM auth_session WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM auth_session")
    suspend fun deleteAll()
}