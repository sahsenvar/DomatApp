package com.domatapp.core.local.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = NSHomeDirectory() + "/domatapp.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath
    )
}
