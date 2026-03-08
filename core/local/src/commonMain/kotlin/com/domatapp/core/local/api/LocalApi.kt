package com.domatapp.core.local.api

import com.domatapp.core.local.database.api.DatabaseApi

/**
 * Unified Local API combining Database and Key-Value store operations.
 * Uses Kotlin delegation pattern for clean separation of concerns.
 *
 * Implementation delegates to storage-specific APIs:
 * - KeyValueApi for DataStore Preferences
 * - DatabaseApi for SQLDelight
 */
interface LocalApi : KeyValueApi, DatabaseApi
