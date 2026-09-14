package com.domatapp.core.config.preferences

/**
 * iOS needs no platform handle: KspPreferences' `dataStorePreferencesPath` ignores the `context`
 * argument on this platform and derives the file path from `NSDocumentDirectory` instead.
 */
actual fun preferencesContext(): Any? = null
