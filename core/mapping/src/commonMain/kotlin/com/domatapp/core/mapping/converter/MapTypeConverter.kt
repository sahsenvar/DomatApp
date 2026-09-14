package com.domatapp.core.mapping.converter

import kotlin.reflect.KClass

/**
 * Bidirectional type converter for mapping between source and target types.
 * Handles null safety automatically - subclasses only implement non-null conversion logic.
 *
 * Example:
 * ```kotlin
 * object StringToIntConverter : MapTypeConverter<String, Int>(String::class, Int::class) {
 *     override fun convertToNonNull(value: String): Int = value.toInt()
 *     override fun convertFromNonNull(value: Int): String = value.toString()
 * }
 * ```
 */
abstract class MapTypeConverter<S : Any, T : Any>(
    val sourceType: KClass<S>,
    val targetType: KClass<T>
) {

    /**
     * Convert non-null source to target.
     */
    abstract fun convertToNonNull(value: S): T

    /**
     * Convert non-null target back to source.
     */
    abstract fun convertFromNonNull(value: T): S

    /**
     * Converts from source type S to target type T (with null handling).
     */
    fun convertTo(value: S?): T? = value?.let { convertToNonNull(it) }

    /**
     * Converts from target type T back to source type S (with null handling).
     */
    fun convertFrom(value: T?): S? = value?.let { convertFromNonNull(it) }
}
