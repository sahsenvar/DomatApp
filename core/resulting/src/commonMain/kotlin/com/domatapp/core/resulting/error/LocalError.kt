package com.domatapp.core.resulting.error

/**
 * Local storage related domain errors.
 * These are infrastructure-level errors that can occur during local database operations
 * (Room, etc.) via core:local module.
 */
sealed class LocalError(message: String? = null, cause: Throwable? = null) : DomainError(message, cause) {

    /**
     * Constraint violation (unique, foreign key, not null, etc.).
     */
    data class ConstraintViolation(
        override val message: String = "Database constraint violation"
    ) : LocalError(message)

    /**
     * Expected record not found.
     */
    data class NotFound(
        override val message: String = "Record not found"
    ) : LocalError(message)

    /**
     * Database file is corrupted or unreadable.
     */
    data class DatabaseCorrupted(
        override val message: String = "Database is corrupted"
    ) : LocalError(message)

    /**
     * Database migration failed.
     */
    data class MigrationFailed(
        val fromVersion: Long,
        val toVersion: Long,
        override val message: String = "Migration failed from version $fromVersion to $toVersion"
    ) : LocalError(message)

    /**
     * Unknown local storage error.
     */
    data class Unknown(
        override val message: String?,
        override val cause: Throwable? = null
    ) : LocalError(message, cause)
}
