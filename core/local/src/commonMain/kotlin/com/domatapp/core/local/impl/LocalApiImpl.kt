package com.domatapp.core.local.impl

import com.domatapp.core.local.api.KeyValueApi
import com.domatapp.core.local.api.LocalApi
import org.koin.core.annotation.Single

/**
 * Unified LocalApi implementation using Kotlin delegation pattern.
 * Delegates Key-Value operations to KeyValueApi and (Future) Database operations to DatabaseApi.
 */
//@Single
class LocalApiImpl(
    keyValueApi: KeyValueApi
) : LocalApi,
    KeyValueApi by keyValueApi
