package com.domatapp.core.resulting.error

/**
 * Base abstract class for all domain errors.
 * All domain-specific errors should extend from this.
 *
 * Note: This is an abstract class (not sealed) to allow feature modules
 * to create their own error hierarchies by extending from DomainError.
 */
abstract class DomainError(message: String? = null, cause: Throwable? = null) : Exception(message, cause)