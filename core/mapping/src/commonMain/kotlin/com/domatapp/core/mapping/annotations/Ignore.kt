package com.domatapp.core.mapping.annotations

/**
 * Ignores automatic mapping for this field.
 *
 * Even if a matching field exists in the target class (by name or via @FieldMap),
 * this field will be treated as if it doesn't exist and will require an external parameter.
 *
 * **Example:**
 * ```kotlin
 * @MapTo(UserDomain::class)
 * data class UserRemote(
 *     @Ignore
 *     val userId: String?  // UserDomain.userId exists but will be ignored
 * )
 *
 * data class UserDomain(val userId: String)
 *
 * // Generated:
 * fun UserRemote.toUserDomain(userId: String): UserDomain = UserDomain(
 *     userId = userId  // External parameter required
 * )
 * ```
 *
 * **Use Cases:**
 * - Force external parameter even when field names match
 * - Override automatic type conversion
 * - Provide custom logic outside of mapper
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Ignore
