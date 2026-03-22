package com.domatapp.feature.auth.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_session")
data class AuthSessionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: Long,
    val createdAt: Long
)