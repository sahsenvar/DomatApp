package com.domatapp.core.remote.impl

import com.domatapp.core.remote.api.RemoteApi
import com.domatapp.core.remote.firestore.api.FirestoreApi
import com.domatapp.core.remote.remoteconfig.api.RemoteConfigApi
import com.domatapp.core.remote.rest.api.RestApi
import com.domatapp.core.remote.socket.api.SocketApi
import org.koin.core.annotation.Single

/**
 * Unified RemoteApi implementation using Kotlin delegation pattern.
 * Delegates REST operations to RestApi, WebSocket operations to SocketApi,
 * Remote Config operations to RemoteConfigApi, and Firestore operations to FirestoreApi.
 */
@Single(binds = [RemoteApi::class])
class RemoteApiImpl(
    restApi: RestApi,
    socketApi: SocketApi,
    remoteConfigApi: RemoteConfigApi,
    firestoreApi: FirestoreApi
) : RemoteApi,
    RestApi by restApi,
    SocketApi by socketApi,
    RemoteConfigApi by remoteConfigApi,
    FirestoreApi by firestoreApi