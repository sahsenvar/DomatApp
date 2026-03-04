package com.domatapp.core.local.factory

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect fun createDataStore(name: String): DataStore<Preferences>

internal const val DATA_STORE_FILE_EXTENSION = ".preferences_pb"
