package com.domatapp.core.remote.api

import com.domatapp.core.remote.rest.api.RestApi
import com.domatapp.core.remote.socket.api.SocketApi

/**
 * Unified Remote API combining REST and WebSocket protocols.
 * Uses Kotlin delegation pattern for clean separation of concerns.
 *
 * Implementation delegates to protocol-specific APIs:
 * - RestApi for HTTP methods (GET, POST, PUT, PATCH, DELETE)
 * - SocketApi for WebSocket communication
 */
interface RemoteApi : RestApi, SocketApi
