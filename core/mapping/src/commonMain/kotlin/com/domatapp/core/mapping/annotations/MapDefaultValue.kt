package com.domatapp.core.mapping.annotations

/**
 * Provides a default value expression for a field when the source field is null.
 *
 * Example:
 * ```kotlin
 * data class OrderDomain(
 *     val id: String,
 *     @MapDefaultValue("Clock.System.now()")
 *     val createdAt: Instant
 * ) : DomainModel
 * ```
 *
 * Generates: `createdAt = sourceCreatedAt ?: Clock.System.now()`
 *
 * @param expression The Kotlin expression to use as default (e.g., "emptyList()", "0", "Clock.System.now()")
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class MapDefaultValue(val expression: String)
