package com.domatapp.shared.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.domatapp.feature.auth.data.datasource.AuthLocalDataSource
import com.domatapp.feature.auth.data.dto.AuthSessionEntity

@Database(entities = [AuthSessionEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authLocalDataSource(): AuthLocalDataSource
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
