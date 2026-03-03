package com.domatapp.core.resulting.error

/**
 * Serialization/Deserialization related domain errors.
 * Each SerializationApi implementation (kotlinx.serialization, Moshi, Gson, etc.)
 * maps its own exceptions to these domain errors.
 */
sealed class SerializationError(
    message: String? = null,
    cause: Throwable? = null
) : DomainError(message, cause) {

    /**
     * Failed to encode/serialize an object to JSON/bytes.
     */
    data class EncodingError(
        override val message: String?,
        override val cause: Throwable? = null
    ) : SerializationError(message, cause)

    /**
     * Failed to decode/deserialize JSON/bytes to an object.
     */
    data class DecodingError(
        override val message: String?,
        override val cause: Throwable? = null
    ) : SerializationError(message, cause)

    /**
     * Missing or unknown fields during serialization.
     */
    data class MissingFieldError(
        val fieldName: String,
        override val cause: Throwable? = null
    ) : SerializationError("Missing required field: $fieldName", cause)

    /**
     * Type mismatch during serialization.
     */
    data class TypeMismatchError(
        override val message: String?,
        override val cause: Throwable? = null
    ) : SerializationError(message, cause)

    /**
     * Unknown serialization error.
     */
    data class Unknown(
        override val message: String?,
        override val cause: Throwable? = null
    ) : SerializationError(message, cause)
}