package com.domatapp.core.config.annotations

/**
 * Saves a value to the key-value store.
 * The type of the parameter determines the type of the value saved.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Save(val key: String)

/**
 * Retrieves a value from the key-value store.
 * The return type of the function determines the type of the value retrieved.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Retrieve(val key: String, val defaultValue: String = "")

/**
 * Removes a value from the key-value store by its key.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Clear(val key: String)

/**
 * Clears all values from the key-value store.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class ClearAll
