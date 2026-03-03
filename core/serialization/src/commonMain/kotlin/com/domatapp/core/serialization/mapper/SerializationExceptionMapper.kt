package com.domatapp.core.serialization.mapper

import com.domatapp.core.resulting.error.SerializationError
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException

/**
 * Maps kotlinx.serialization exceptions to SerializationError domain errors.
 */
@OptIn(ExperimentalSerializationApi::class)
fun Exception.toSerializationError(): SerializationError {
    return when (this) {
        // Already a SerializationError
        is SerializationError -> this

        // kotlinx.serialization specific exceptions
        is MissingFieldException -> SerializationError.MissingFieldError(
            fieldName = this.missingFields.firstOrNull() ?: "unknown",
            cause = this
        )

        is SerializationException -> {
            val message = this.message ?: "Serialization error"
            when {
                message.contains("decode", ignoreCase = true) ||
                        message.contains("parse", ignoreCase = true) ->
                    SerializationError.DecodingError(message, this)

                message.contains("encode", ignoreCase = true) ->
                    SerializationError.EncodingError(message, this)

                message.contains("type", ignoreCase = true) ||
                        message.contains("expected", ignoreCase = true) ->
                    SerializationError.TypeMismatchError(message, this)

                else -> SerializationError.Unknown(message, this)
            }
        }

        // Unknown exceptions
        else -> SerializationError.Unknown(
            message = message ?: "Unknown serialization error",
            cause = this
        )
    }
}
