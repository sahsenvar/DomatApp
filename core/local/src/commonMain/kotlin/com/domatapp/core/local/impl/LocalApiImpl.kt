package com.domatapp.core.local.impl

import com.domatapp.core.local.api.KeyValueApi
import com.domatapp.core.local.api.LocalApi
import com.domatapp.core.local.database.api.DatabaseApi
import org.koin.core.annotation.Single

/**
 * Unified LocalApi implementation using Kotlin delegation pattern.
 * Delegates Key-Value operations to KeyValueApi and Database operations to DatabaseApi.
 */
@Single(binds = [LocalApi::class])
class LocalApiImpl(
    keyValueApi: KeyValueApi,
    databaseApi: DatabaseApi
) : LocalApi,
    KeyValueApi by keyValueApi,
    DatabaseApi by databaseApi
