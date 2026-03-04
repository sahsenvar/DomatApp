package com.domatapp.core.remote.annotations

/**
 * Fetches and activates the remote config values.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class FetchRemoteConfig

/**
 * Retrieves a value from the remote config by its key.
 * The return type of the function determines the type of the value retrieved.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class GetRemoteConfig(val key: String)

/**
 * Observes a value from the remote config by its key.
 * The return type of the function must be a Flow<T>.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class ObserveRemoteConfig(val key: String)
