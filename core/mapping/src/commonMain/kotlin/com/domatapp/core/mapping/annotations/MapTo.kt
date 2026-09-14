package com.domatapp.core.mapping.annotations

import kotlin.reflect.KClass

/**
 * Marks a class for automatic mapping code generation to the specified target class.
 *
 * Example:
 * ```kotlin
 * @MapTo(AuthSession::class)
 * data class RemoteUserDto(val userId: String?) : RemoteModel
 * ```
 *
 * Generates:
 * ```kotlin
 * fun RemoteUserDto.toAuthSession(): AuthSession { ... }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class MapTo(val target: KClass<*>)
