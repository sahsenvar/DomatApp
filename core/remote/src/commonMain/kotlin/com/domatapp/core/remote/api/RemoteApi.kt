package com.domatapp.core.remote.api

import com.domatapp.core.remote.firestore.api.FirestoreApi
import com.domatapp.core.remote.remoteconfig.api.RemoteConfigApi
import com.domatapp.core.remote.rest.api.RestApi
import com.domatapp.core.remote.socket.api.SocketApi

/**
 * Unified Remote API combining REST, WebSocket, Remote Config, and Firestore protocols.
 * Uses Kotlin delegation pattern for clean separation of concerns.
 *
 * Implementation delegates to protocol-specific APIs:
 * - RestApi for HTTP methods (GET, POST, PUT, PATCH, DELETE)
 * - SocketApi for WebSocket communication
 * - RemoteConfigApi for Firebase Remote Config
 * - FirestoreApi for Firestore document CRUD and realtime observation
 */
interface RemoteApi : RestApi, SocketApi, RemoteConfigApi, FirestoreApi

