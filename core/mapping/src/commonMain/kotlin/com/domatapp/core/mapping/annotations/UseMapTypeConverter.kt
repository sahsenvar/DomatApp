package com.domatapp.core.mapping.annotations

import com.domatapp.core.mapping.converter.MapTypeConverter
import kotlin.reflect.KClass

/**
 * Specifies a custom MapTypeConverter for field mapping.
 *
 * Example:
 * ```kotlin
 * data class UserRemote(
 *     @UseMapTypeConverter(StringToInstantConverter::class)
 *     val createdAt: String?
 * ) : RemoteModel
 * ```
 *
 * @param converter The MapTypeConverter class to use for this field
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class UseMapTypeConverter(val converter: KClass<out MapTypeConverter<*, *>>)
