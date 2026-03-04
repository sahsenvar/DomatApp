package com.domatapp.core.remote.remoteconfig.impl

import com.domatapp.core.remote.remoteconfig.api.RemoteConfigApi
import com.domatapp.core.serialization.api.SerializationApi
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

/**
 * Firebase implementation of RemoteConfigApi.
 * Provides real-time flows by triggering updates upon successful fetchAndActivate.
 */
@Single
class FirebaseRemoteConfigApi(
    private val serializationApi: SerializationApi
) : RemoteConfigApi {

    private val remoteConfig by lazy { Firebase.remoteConfig }
    
    // A shared flow to notify observers when configurations are re-fetched and activated.
    private val updateTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override suspend fun fetchAndActivate(): Boolean {
        return try {
            val activated = remoteConfig.fetchAndActivate()
            if (activated) {
                updateTrigger.tryEmit(Unit)
            }
            activated
        } catch (e: Exception) {
            false
        }
    }

    override fun getString(key: String): String {
        return remoteConfig.getValue(key).asString()
    }

    override fun getBoolean(key: String): Boolean {
        return remoteConfig.getValue(key).asBoolean()
    }

    override fun getLong(key: String): Long {
        return remoteConfig.getValue(key).asLong()
    }

    override fun getDouble(key: String): Double {
        return remoteConfig.getValue(key).asDouble()
    }

    override fun <T : Any> getSerializable(key: String, type: KClass<T>): T {
        val json = getString(key)
        return serializationApi.deserialize(json, type)
    }

    override fun observeString(key: String): Flow<String> {
        return updateTrigger
            .onStart { emit(Unit) }
            .map { getString(key) }
            .distinctUntilChanged()
    }

    override fun observeBoolean(key: String): Flow<Boolean> {
        return updateTrigger
            .onStart { emit(Unit) }
            .map { getBoolean(key) }
            .distinctUntilChanged()
    }

    override fun observeLong(key: String): Flow<Long> {
        return updateTrigger
            .onStart { emit(Unit) }
            .map { getLong(key) }
            .distinctUntilChanged()
    }

    override fun observeDouble(key: String): Flow<Double> {
        return updateTrigger
            .onStart { emit(Unit) }
            .map { getDouble(key) }
            .distinctUntilChanged()
    }

    override fun <T : Any> observeSerializable(key: String, type: KClass<T>): Flow<T> {
        return updateTrigger
            .onStart { emit(Unit) }
            .map { getSerializable(key, type) }
            .distinctUntilChanged()
    }
}
