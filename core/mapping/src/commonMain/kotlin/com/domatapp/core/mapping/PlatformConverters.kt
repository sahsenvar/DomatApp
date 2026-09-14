package com.domatapp.core.mapping

/**
 * Platform-specific converter registration.
 * Each platform implements this to register its own converters.
 */
expect object PlatformConverters {
    /**
     * Registers all platform-specific type converters.
     * Called once during app initialization.
     */
    fun register()
}
