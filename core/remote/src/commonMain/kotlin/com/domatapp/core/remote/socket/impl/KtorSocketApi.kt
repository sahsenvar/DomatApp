package com.domatapp.core.remote.socket.impl

import com.domatapp.core.remote.mapper.toRemoteError
import com.domatapp.core.remote.socket.api.SocketApi
import com.domatapp.core.serialization.api.SerializationApi
import io.ktor.client.HttpClient
import org.koin.core.annotation.Single
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlin.reflect.KClass

/**
 * Ktor WebSocket implementation of SocketApi.
 */
@Single
class KtorSocketApi(
    private val client: HttpClient,
    private val serializer: SerializationApi
) : SocketApi {

    private val connections = mutableMapOf<String, DefaultClientWebSocketSession>()

    override fun <T : Any> subscribe(
        path: String,
        messageType: KClass<T>
    ): Flow<T> = flow {
        client.webSocket(path) {
            // Store active connection
            connections[path] = this

            // Listen for incoming messages
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        val message = serializer.deserialize(text, messageType)
                        emit(message)
                    }

                    is Frame.Binary -> {
                        val bytes = frame.readBytes()
                        val message = serializer.deserializeFromByteArray(bytes, messageType)
                        emit(message)
                    }

                    else -> {
                        // Ignore other frame types (Close, Ping, Pong)
                    }
                }
            }
        }
    }
        .catch { e -> throw e.toRemoteError() }
        .onCompletion {
            // Clean up connection on flow completion
            connections.remove(path)
        }

    override suspend fun <T : Any> send(
        path: String,
        message: Any,
        responseType: KClass<T>
    ): T = try {
        val connection = connections[path]
            ?: throw IllegalStateException("No active WebSocket connection for path: $path. Call subscribe() first.")

        // Send message
        val serialized = serializer.serialize(message)
        connection.send(Frame.Text(serialized))

        // Wait for response
        val response = connection.incoming.receive()
        when (response) {
            is Frame.Text -> {
                serializer.deserialize(response.readText(), responseType)
            }
            is Frame.Binary -> {
                serializer.deserializeFromByteArray(response.readBytes(), responseType)
            }
            else -> {
                throw IllegalStateException("Unexpected frame type: ${response.frameType}")
            }
        }
    } catch (e: Exception) {
        throw e.toRemoteError()
    }

    override fun disconnect(path: String) {
        connections.remove(path)?.cancel()
    }
}