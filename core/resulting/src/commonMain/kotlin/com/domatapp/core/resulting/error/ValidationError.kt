package com.domatapp.core.resulting.error

/**
 * Validation-related domain errors.
 * Used for input validation, business rule violations, etc.
 */
sealed class ValidationError(message: String? = null) : DomainError(message) {

    /**
     * Required field is missing or empty.
     */
    data class RequiredField(val fieldName: String) : ValidationError("$fieldName is required")

    /**
     * Invalid format (e.g., email, phone number).
     */
    data class InvalidFormat(val fieldName: String) : ValidationError("$fieldName has invalid format")

    /**
     * Value out of range.
     */
    data class OutOfRange(val fieldName: String, val min: Any?, val max: Any?) : ValidationError("$fieldName must be between $min and $max")

    /**
     * Custom validation error.
     */
    data class Custom(override val message: String?) : ValidationError(message)
}