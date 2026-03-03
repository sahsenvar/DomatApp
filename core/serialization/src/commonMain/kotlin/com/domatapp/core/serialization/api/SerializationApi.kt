package com.domatapp.core.serialization.api

import kotlin.reflect.KClass

/**
 * Serialization abstraction to decouple from specific serialization library.
 * Allows swapping between kotlinx.serialization, Moshi, Gson, etc.
 */
interface SerializationApi {

    /**
     * Serialize an object to JSON string
     */
    fun <T : Any> serialize(value: T): String

    /**
     * Deserialize JSON string to object
     */
    fun <T : Any> deserialize(json: String, type: KClass<T>): T

    /**
     * Serialize an object to ByteArray
     */
    fun <T : Any> serializeToByteArray(value: T): ByteArray

    /**
     * Deserialize ByteArray to object
     */
    fun <T : Any> deserializeFromByteArray(bytes: ByteArray, type: KClass<T>): T
}
