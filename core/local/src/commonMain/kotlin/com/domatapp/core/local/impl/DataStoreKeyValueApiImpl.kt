package com.domatapp.core.local.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.domatapp.core.local.api.KeyValueApi
import com.domatapp.core.local.factory.createDataStore
import com.domatapp.core.serialization.api.SerializationApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

class DataStoreKeyValueApiImpl(
    private val serializationApi: SerializationApi,
    private val name: String
) : KeyValueApi {
    private val dataStore: DataStore<Preferences> by lazy { createDataStore("auth") }

    // retrieve =======================================================================================================
    override fun retrieveString(key: String): Flow<String?> = dataStore.data
        .map { it[stringPreferencesKey(key)] }

    override fun retrieveInt(key: String): Flow<Int?> = dataStore.data
        .map { it[intPreferencesKey(key)] }

    override fun retrieveDouble(key: String): Flow<Double?> = dataStore.data
        .map { it[doublePreferencesKey(key)] }

    override fun retrieveBoolean(key: String): Flow<Boolean?> = dataStore.data
        .map { it[booleanPreferencesKey(key)] }

    override fun retrieveFloat(key: String): Flow<Float?> = dataStore.data
        .map { it[floatPreferencesKey(key)] }

    override fun retrieveLong(key: String): Flow<Long?> = dataStore.data
        .map { it[longPreferencesKey(key)] }

    override fun retrieveStringSet(key: String): Flow<Set<String>?> = dataStore.data
        .map { it[stringSetPreferencesKey(key)] }

    override fun retrieveByteArray(key: String): Flow<ByteArray?> = dataStore.data
        .map { it[byteArrayPreferencesKey(key)] }

    override fun <T : Any> retrieveSerializable(key: String, kClass: KClass<T>): Flow<T?> =
        dataStore.data
            .map { it[stringPreferencesKey(key)] }
            .map { it?.let { serializationApi.deserialize(it, kClass) } }

    // SAVE ===========================================================================================================
    override suspend fun saveInt(key: String, value: Int) {
        dataStore.edit { it[intPreferencesKey(key)] = value }
    }

    override suspend fun saveString(key: String, value: String) {
        dataStore.edit { it[stringPreferencesKey(key)] = value }
    }

    override suspend fun saveDouble(key: String, value: Double) {
        dataStore.edit { it[doublePreferencesKey(key)] = value }
    }

    override suspend fun saveBoolean(key: String, value: Boolean) {
        dataStore.edit { it[booleanPreferencesKey(key)] = value }
    }

    override suspend fun saveFloat(key: String, value: Float) {
        dataStore.edit { it[floatPreferencesKey(key)] = value }
    }

    override suspend fun saveLong(key: String, value: Long) {
        dataStore.edit { it[longPreferencesKey(key)] = value }
    }

    override suspend fun saveStringSet(key: String, value: Set<String>) {
        dataStore.edit { it[stringSetPreferencesKey(key)] = value }
    }

    override suspend fun saveByteArray(key: String, value: ByteArray) {
        dataStore.edit { it[byteArrayPreferencesKey(key)] = value }
    }

    override suspend fun <T : Any> saveSerializable(key: String, value: T) {
        dataStore.edit { it[stringPreferencesKey(key)] = serializationApi.serialize(value) }
    }

    // REMOVE ==========================================================================================================
    override suspend fun eraseInt(key: String) {
        dataStore.edit { it.remove(intPreferencesKey(key)) }
    }

    override suspend fun eraseString(key: String) {
        dataStore.edit { it.remove(stringPreferencesKey(key)) }
    }

    override suspend fun eraseDouble(key: String) {
        dataStore.edit { it.remove(doublePreferencesKey(key)) }
    }

    override suspend fun eraseBoolean(key: String) {
        dataStore.edit { it.remove(booleanPreferencesKey(key)) }
    }

    override suspend fun eraseFloat(key: String) {
        dataStore.edit { it.remove(floatPreferencesKey(key)) }
    }

    override suspend fun eraseLong(key: String) {
        dataStore.edit { it.remove(longPreferencesKey(key)) }
    }

    override suspend fun eraseStringSet(key: String) {
        dataStore.edit { it.remove(stringSetPreferencesKey(key)) }
    }

    override suspend fun eraseByteArray(key: String) {
        dataStore.edit { it.remove(byteArrayPreferencesKey(key)) }
    }

    override suspend fun eraseSerializable(key: String) {
        dataStore.edit { it.remove(stringPreferencesKey(key)) }
    }

    // Clear ===========================================================================================================
    override suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
