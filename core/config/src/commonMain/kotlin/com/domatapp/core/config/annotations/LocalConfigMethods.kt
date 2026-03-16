package com.domatapp.core.config.annotations

/**
 * Saves a value to the local key-value store (DataStore).
 * The type of the parameter determines the type of the value saved.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class SaveLocalConfig(val key: String)

/**
 * Retrieves a single value from the local key-value store (DataStore).
 * The function must be `suspend` and return a non-Flow type (uses `first()`).
 *
 * Compile-time error if return type is Flow — use @ObserveLocalConfig instead.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class RetrieveLocalConfig(val key: String)

/**
 * Observes a value from the local key-value store (DataStore) as a Flow.
 * The function must return Flow<T>.
 *
 * Compile-time error if return type is not Flow — use @RetrieveLocalConfig instead.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class ObserveLocalConfig(val key: String)

/**
 * Removes a value from the local key-value store by its key.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class ClearLocalConfig(val key: String)

/**
 * Clears all values from the local key-value store.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class ClearAllLocalConfig
