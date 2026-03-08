package com.domatapp.core.local.database.factory

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.domatapp.core.local.database.DomatAppDatabase

actual fun createSqlDriver(databaseName: String): SqlDriver {
    return NativeSqliteDriver(
        schema = DomatAppDatabase.Schema,
        name = "$databaseName.db"
    )
}
