package com.domatapp.core.config.annotations

/**
 * Marks an interface for KSP code generation of ConfigSource implementations.
 * The processor analyzes method annotations to determine which backends (DataStore/RemoteConfig)
 * are needed and injects only required dependencies.
 *
 * @param name The DataStore store name. Required when LocalConfig annotations (@SaveLocalConfig, @RetrieveLocalConfig, etc.) are used.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ConfigSource(val name: String = "")
