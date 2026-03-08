package com.domatapp.core.local.database.factory

import app.cash.sqldelight.db.SqlDriver

expect fun createSqlDriver(databaseName: String): SqlDriver
