package com.domatapp.core.remote.mapper

import com.domatapp.core.resulting.error.DomainError
import com.domatapp.core.resulting.error.RemoteError
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import kotlinx.io.IOException

/**
 * Maps Ktor exceptions to RemoteError domain errors.
 *
 * Note: SerializationError and other DomainErrors are NOT mapped here.
 * They are already domain errors and should be thrown as-is.
 */
fun Throwable.toRemoteError(): DomainError {
    return when (this) {
        // Already a DomainError (SerializationError, RemoteError, etc.)
        is DomainError -> this

        // Ktor-specific exceptions
        is ConnectTimeoutException -> RemoteError.Timeout()
        is SocketTimeoutException -> RemoteError.Timeout()
        is HttpRequestTimeoutException -> RemoteError.Timeout()

        is IOException -> RemoteError.NoConnection()

        is ClientRequestException -> {
            val statusCode = response.status.value
            RemoteError.ClientError(
                code = statusCode,
                message = "Client error: $statusCode"
            )
        }

        is ServerResponseException -> {
            val statusCode = response.status.value
            RemoteError.ServerError(
                code = statusCode,
                message = "Server error: $statusCode"
            )
        }

        is ResponseException -> {
            val statusCode = response.status.value
            if (statusCode in 400..499) {
                RemoteError.ClientError(code = statusCode, message = message)
            } else {
                RemoteError.ServerError(code = statusCode, message = message)
            }
        }

        // Unknown error
        else -> RemoteError.Unknown(
            message = message ?: "Unknown remote error",
            cause = this
        )
    }
}
