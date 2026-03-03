package com.domatapp.core.serialization.impl

import com.domatapp.core.serialization.api.SerializationApi
import com.domatapp.core.serialization.mapper.toSerializationError
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

/**
 * kotlinx.serialization implementation of SerializationApi.
 * Maps kotlinx.serialization exceptions to SerializationError domain errors.
 */
@Single(binds = [SerializationApi::class])
class KotlinxSerializationApi : SerializationApi {

    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> serialize(value: T): String = try {
        @Suppress("UNCHECKED_CAST")
        val serializer = value::class.serializer() as KSerializer<T>
        json.encodeToString(serializer, value)
    } catch (e: Exception) {
        throw e.toSerializationError()
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> deserialize(json: String, type: KClass<T>): T = try {
        val serializer = type.serializer()
        @Suppress("UNCHECKED_CAST")
        this.json.decodeFromString(serializer, json)
    } catch (e: Exception) {
        throw e.toSerializationError()
    }

    override fun <T : Any> serializeToByteArray(value: T): ByteArray = try {
        serialize(value).encodeToByteArray()
    } catch (e: Exception) {
        throw e.toSerializationError()
    }

    override fun <T : Any> deserializeFromByteArray(bytes: ByteArray, type: KClass<T>): T = try {
        deserialize(bytes.decodeToString(), type)
    } catch (e: Exception) {
        throw e.toSerializationError()
    }
}
