package com.domatapp.core.remote.impl

import com.domatapp.core.remote.api.RemoteApi
import com.domatapp.core.remote.rest.api.RestApi
import com.domatapp.core.remote.socket.api.SocketApi
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

/**
 * Unified RemoteApi implementation using Kotlin delegation pattern.
 * Delegates REST operations to RestApi and WebSocket operations to SocketApi.
 */
@Single
class RemoteApiImpl(
    restApi: RestApi,
    socketApi: SocketApi
) : RemoteApi,
    RestApi by restApi,
    SocketApi by socketApi