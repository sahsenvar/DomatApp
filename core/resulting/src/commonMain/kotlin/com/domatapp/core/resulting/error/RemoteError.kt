package com.domatapp.core.resulting.error

/**
 * Remote API related domain errors.
 * These are infrastructure-level errors that can occur during remote API operations
 * (REST, WebSocket, GraphQL, etc.) via core:remote module.
 */
sealed class RemoteError(message: String? = null, cause: Throwable? = null) : DomainError(message, cause) {

    /**
     * No internet connection available.
     */
    data class NoConnection(override val message: String = "No internet connection") : RemoteError(message)

    /**
     * Request timeout.
     */
    data class Timeout(override val message: String = "Request timed out") : RemoteError(message)

    /**
     * Server error (5xx status codes).
     */
    data class ServerError(
        val code: Int,
        override val message: String?
    ) : RemoteError(message)

    /**
     * Client error (4xx status codes).
     */
    data class ClientError(
        val code: Int,
        override val message: String?
    ) : RemoteError(message)

    /**
     * Unknown remote error.
     */
    data class Unknown(
        override val message: String?,
        override val cause: Throwable? = null
    ) : RemoteError(message, cause)
}
