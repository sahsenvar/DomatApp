package com.domatapp.core.remote.socket.api

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

/**
 * WebSocket API abstraction for real-time communication.
 * Implementations: KtorSocketApi, etc.
 */
interface SocketApi {

    /**
     * Subscribe to WebSocket messages at given path.
     * Returns a Flow that emits messages as they arrive.
     *
     * @param path WebSocket endpoint path
     * @param messageType Type of messages to receive
     * @return Flow of messages
     */
    fun <T : Any> subscribe(
        path: String,
        messageType: KClass<T>
    ): Flow<T>

    /**
     * Send a message through WebSocket and wait for response.
     *
     * @param path WebSocket endpoint path
     * @param message Message to send
     * @param responseType Expected response type
     * @return Response from server
     */
    suspend fun <T : Any> send(
        path: String,
        message: Any,
        responseType: KClass<T>
    ): T

    /**
     * Disconnect from WebSocket at given path.
     *
     * @param path WebSocket endpoint path
     */
    fun disconnect(path: String)
}