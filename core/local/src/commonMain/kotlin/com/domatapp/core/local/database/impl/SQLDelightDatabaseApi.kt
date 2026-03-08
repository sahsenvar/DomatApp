package com.domatapp.core.local.database.impl

import app.cash.sqldelight.db.SqlDriver
import com.domatapp.core.local.database.api.DatabaseApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

/**
 * SQLDelight-based DatabaseApi implementation.
 * Provides transaction support via SqlDriver's transactionWithResult.
 */
@Single(binds = [DatabaseApi::class])
class SQLDelightDatabaseApi(
    private val driver: SqlDriver
) : DatabaseApi {

    private val transactionMutex = Mutex()

    override suspend fun <T> transaction(body: suspend () -> T): T {
        return transactionMutex.withLock {
            driver.execute(null, "BEGIN TRANSACTION", 0)
            try {
                val result = body()
                driver.execute(null, "COMMIT", 0)
                result
            } catch (e: Exception) {
                driver.execute(null, "ROLLBACK", 0)
                throw e
            }
        }
    }

    override fun getDriver(): SqlDriver = driver
}
