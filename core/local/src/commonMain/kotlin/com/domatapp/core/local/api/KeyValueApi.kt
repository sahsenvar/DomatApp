package com.domatapp.core.local.api

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

/**
 * API for Key-Value store operations (Multiplatform Settings).
 */
interface KeyValueApi {
    // RETRIEVE ============================================================================================================
    fun retrieveString(key: String): Flow<String?>
    fun retrieveInt(key: String): Flow<Int?>
    fun retrieveDouble(key: String): Flow<Double?>
    fun retrieveBoolean(key: String): Flow<Boolean?>
    fun retrieveFloat(key: String): Flow<Float?>
    fun retrieveLong(key: String): Flow<Long?>
    fun retrieveStringSet(key: String): Flow<Set<String>?>
    fun retrieveByteArray(key: String): Flow<ByteArray?>
    fun <T : Any> retrieveSerializable(key: String, kClass: KClass<T>): Flow<T?>

    // SAVE ===========================================================================================================
    suspend fun saveInt(key: String, value: Int)
    suspend fun saveString(key: String, value: String)
    suspend fun saveDouble(key: String, value: Double)
    suspend fun saveBoolean(key: String, value: Boolean)
    suspend fun saveFloat(key: String, value: Float)
    suspend fun saveLong(key: String, value: Long)
    suspend fun saveStringSet(key: String, value: Set<String>)
    suspend fun saveByteArray(key: String, value: ByteArray)
    suspend fun <T : Any> saveSerializable(key: String, value: T)

    // ERASE ==========================================================================================================
    suspend fun eraseInt(key: String)
    suspend fun eraseString(key: String)
    suspend fun eraseDouble(key: String)
    suspend fun eraseBoolean(key: String)
    suspend fun eraseFloat(key: String)
    suspend fun eraseLong(key: String)
    suspend fun eraseStringSet(key: String)
    suspend fun eraseByteArray(key: String)
    suspend fun eraseSerializable(key: String)

    // CLEAR
    suspend fun clearAll()
}
