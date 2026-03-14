package com.domatapp.core.remote.remoteconfig

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

/**
 * Firebase-based Remote Config client.
 * Provides real-time flows by triggering updates upon successful fetchAndActivate.
 */
@Single
class FirebaseRemoteConfigClient(
    private val json: Json
) {

    private val remoteConfig by lazy { Firebase.remoteConfig }

    // A shared flow to notify observers when configurations are re-fetched and activated.
    private val updateTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    suspend fun fetchAndActivate(): Boolean {
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

    fun getString(key: String): String {
        return remoteConfig.getValue(key).asString()
    }

    fun getBoolean(key: String): Boolean {
        return remoteConfig.getValue(key).asBoolean()
    }

    fun getLong(key: String): Long {
        return remoteConfig.getValue(key).asLong()
    }

    fun getDouble(key: String): Double {
        return remoteConfig.getValue(key).asDouble()
    }

    @OptIn(InternalSerializationApi::class)
    fun <T : Any> getSerializable(key: String, type: KClass<T>): T {
        val jsonString = getString(key)
        return json.decodeFromString(type.serializer(), jsonString)
    }

    fun observeString(key: String): Flow<String> {
        return updateTrigger
            .onStart { emit(Unit) }
            .map { getString(key) }
            .distinctUntilChanged()
    }

    fun observeBoolean(key: String): Flow<Boolean> {
        return updateTrigger
            .onStart { emit(Unit) }
            .map { getBoolean(key) }
            .distinctUntilChanged()
    }

    fun observeLong(key: String): Flow<Long> {
        return updateTrigger
            .onStart { emit(Unit) }
            .map { getLong(key) }
            .distinctUntilChanged()
    }

    fun observeDouble(key: String): Flow<Double> {
        return updateTrigger
            .onStart { emit(Unit) }
            .map { getDouble(key) }
            .distinctUntilChanged()
    }

    fun <T : Any> observeSerializable(key: String, type: KClass<T>): Flow<T> {
        return updateTrigger
            .onStart { emit(Unit) }
            .map { getSerializable(key, type) }
            .distinctUntilChanged()
    }
}

/**
 * Inline extension to get a custom serializable object without explicitly passing the KClass.
 */
inline fun <reified T : Any> FirebaseRemoteConfigClient.getSerializable(key: String): T =
    getSerializable(key, T::class)

/**
 * Inline extension to observe a custom serializable object without explicitly passing the KClass.
 */
inline fun <reified T : Any> FirebaseRemoteConfigClient.observeSerializable(key: String): Flow<T> =
    observeSerializable(key, T::class)
