package com.domatapp.core.local.api

/**
 * Unified Local API combining Database and Key-Value store operations.
 * Uses Kotlin delegation pattern for clean separation of concerns.
 *
 * Implementation delegates to storage-specific APIs:
 * - KeyValueApi for Multiplatform Settings
 * - (Future) DatabaseApi for SQLDelight
 */
interface LocalApi : KeyValueApi
