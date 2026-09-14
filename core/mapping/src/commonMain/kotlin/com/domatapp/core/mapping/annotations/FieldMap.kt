package com.domatapp.core.mapping.annotations

import kotlin.reflect.KClass

/**
 * Maps a field to a different field name in the target class.
 *
 * **Single Target Example:**
 * ```kotlin
 * @MapTo(UserDomain::class)
 * data class UserRemote(
 *     @FieldMap(fieldName = "id")
 *     val userId: String?
 * ) : RemoteModel
 * ```
 * Maps `userId` -> `id` in UserDomain.
 *
 * **Multiple Targets Example (REQUIRED):**
 * ```kotlin
 * @MapTo(UserDomain::class)
 * @MapTo(ProfileDomain::class)
 * data class UserRemote(
 *     @FieldMap(fieldName = "id", targetClass = UserDomain::class)
 *     @FieldMap(fieldName = "remoteId", targetClass = ProfileDomain::class)
 *     val remoteId: Int?
 * )
 * ```
 * - UserDomain mapping: `remoteId` -> `id`
 * - ProfileDomain mapping: `remoteId` -> `remoteId`
 *
 * @param fieldName The target field name in the destination class
 * @param targetClass REQUIRED when multiple @MapTo exist: specify which target class this mapping applies to
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class FieldMap(
    val fieldName: String,
    val targetClass: KClass<*> = Nothing::class
)
