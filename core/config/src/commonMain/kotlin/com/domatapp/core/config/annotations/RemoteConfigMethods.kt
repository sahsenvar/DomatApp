package com.domatapp.core.config.annotations

/**
 * Retrieves a value from the remote config by its key.
 * Automatically calls fetchAndActivate() before retrieving.
 * The function must be `suspend` and return a non-Flow type.
 *
 * Compile-time error if return type is Flow — use @ObserveRemoteConfig instead.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class RetrieveRemoteConfig(val key: String)

/**
 * Observes a value from the remote config by its key as a Flow.
 * Automatically calls fetchAndActivate() on each emission.
 * The function must return Flow<T>.
 *
 * Compile-time error if return type is not Flow — use @RetrieveRemoteConfig instead.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class ObserveRemoteConfig(val key: String)
