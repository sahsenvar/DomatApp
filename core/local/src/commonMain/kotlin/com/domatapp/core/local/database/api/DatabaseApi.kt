package com.domatapp.core.local.database.api

import app.cash.sqldelight.db.SqlDriver

/**
 * API for database operations (SQLDelight).
 * Provides transaction support and driver access for generated Queries objects.
 *
 * SQLDelight generates typed query classes, so generic CRUD methods are not needed.
 * Feature modules inject DomatAppDatabase to access their own *Queries objects.
 */
interface DatabaseApi {
    suspend fun <T> transaction(body: suspend () -> T): T
    fun getDriver(): SqlDriver
}
