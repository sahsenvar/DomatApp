package com.domatapp.core.mapping.annotations

import kotlin.reflect.KClass

/**
 * Marks a class for automatic reverse mapping code generation from the specified source class.
 *
 * Example:
 * ```kotlin
 * @MapFrom(AuthSession::class)
 * data class RemoteUserDto(val userId: String?) : RemoteModel
 * ```
 *
 * Generates:
 * ```kotlin
 * fun AuthSession.toRemoteUserDto(): RemoteUserDto { ... }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class MapFrom(val source: KClass<*>)
