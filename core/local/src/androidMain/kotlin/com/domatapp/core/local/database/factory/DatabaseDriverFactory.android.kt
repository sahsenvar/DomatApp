package com.domatapp.core.local.database.factory

import android.annotation.SuppressLint
import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.domatapp.core.local.database.DomatAppDatabase

@SuppressLint("StaticFieldLeak")
object DatabaseContextHolder {
    lateinit var context: Context
}

actual fun createSqlDriver(databaseName: String): SqlDriver {
    return AndroidSqliteDriver(
        schema = DomatAppDatabase.Schema,
        context = DatabaseContextHolder.context,
        name = "$databaseName.db"
    )
}
