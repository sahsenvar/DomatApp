package com.domatapp.core.remote.remoteconfig.api

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

/**
 * Remote Config API abstraction for fetching and reading remote configuration values.
 * Implementations: FirebaseRemoteConfigApi, etc.
 */
interface RemoteConfigApi {

    /**
     * Fetches and activates the remote config values.
     * @return true if successful and activated, false otherwise.
     */
    suspend fun fetchAndActivate(): Boolean

    /**
     * Retrieves a string value for the given key.
     */
    fun getString(key: String): String

    /**
     * Retrieves a boolean value for the given key.
     */
    fun getBoolean(key: String): Boolean

    /**
     * Retrieves a long value for the given key.
     */
    fun getLong(key: String): Long

    /**
     * Retrieves a double value for the given key.
     */
    fun getDouble(key: String): Double

    /**
     * Retrieves a custom serializable object for the given key.
     * The underlying representation must be a JSON string.
     */
    fun <T : Any> getSerializable(key: String, type: KClass<T>): T

    /**
     * Observes a string value for the given key.
     * Emits the current value and any subsequent changes when configs are re-fetched.
     */
    fun observeString(key: String): Flow<String>

    /**
     * Observes a boolean value for the given key.
     * Emits the current value and any subsequent changes when configs are re-fetched.
     */
    fun observeBoolean(key: String): Flow<Boolean>

    /**
     * Observes a long value for the given key.
     * Emits the current value and any subsequent changes when configs are re-fetched.
     */
    fun observeLong(key: String): Flow<Long>

    /**
     * Observes a double value for the given key.
     * Emits the current value and any subsequent changes when configs are re-fetched.
     */
    fun observeDouble(key: String): Flow<Double>

    /**
     * Observes a custom serializable object for the given key.
     * Emits the current value and any subsequent changes when configs are re-fetched.
     * The underlying representation must be a JSON string.
     */
    fun <T : Any> observeSerializable(key: String, type: KClass<T>): Flow<T>
}

/**
 * Inline extension to get a custom serializable object without explicitly passing the KClass.
 */
inline fun <reified T : Any> RemoteConfigApi.getSerializable(key: String): T =
    getSerializable(key, T::class)

/**
 * Inline extension to observe a custom serializable object without explicitly passing the KClass.
 */
inline fun <reified T : Any> RemoteConfigApi.observeSerializable(key: String): Flow<T> =
    observeSerializable(key, T::class)
