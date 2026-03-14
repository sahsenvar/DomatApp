package com.domatapp.core.local.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.domatapp.core.local.database.factory.DatabaseContextHolder

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = DatabaseContextHolder.context.applicationContext
    val dbFile = appContext.getDatabasePath("domatapp.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
