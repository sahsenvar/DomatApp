package com.domatapp.core.local.database.mapper

import com.domatapp.core.resulting.error.DomainError
import com.domatapp.core.resulting.error.LocalError

/**
 * Maps SQLite/database exceptions to LocalError domain errors.
 *
 * Note: DomainErrors are NOT mapped here.
 * They are already domain errors and should be thrown as-is.
 */
fun Throwable.toLocalError(): DomainError {
    return when {
        // Already a DomainError (LocalError, RemoteError, etc.)
        this is DomainError -> this

        // SQLite constraint violations (unique, foreign key, not null, check)
        message?.contains("constraint", ignoreCase = true) == true ||
            message?.contains("UNIQUE", ignoreCase = true) == true ||
            message?.contains("FOREIGN KEY", ignoreCase = true) == true ||
            message?.contains("NOT NULL", ignoreCase = true) == true -> {
            LocalError.ConstraintViolation(
                message = message ?: "Database constraint violation"
            )
        }

        // Corrupted or missing database structures
        message?.contains("no such table", ignoreCase = true) == true ||
            message?.contains("database disk image is malformed", ignoreCase = true) == true ||
            message?.contains("file is not a database", ignoreCase = true) == true -> {
            LocalError.DatabaseCorrupted(
                message = message ?: "Database is corrupted"
            )
        }

        // Unknown error
        else -> LocalError.Unknown(
            message = message ?: "Unknown local storage error",
            cause = this
        )
    }
}
