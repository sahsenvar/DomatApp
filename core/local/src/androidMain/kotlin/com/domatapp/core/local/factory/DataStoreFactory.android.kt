package com.domatapp.core.local.factory

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

@SuppressLint("StaticFieldLeak")
object DataStoreContextHolder {
    lateinit var context: Context
}

actual fun createDataStore(name: String): DataStore<Preferences> {
    val path = DataStoreContextHolder.context.filesDir.resolve(name + DATA_STORE_FILE_EXTENSION).absolutePath
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { path.toPath() }
    )
}
