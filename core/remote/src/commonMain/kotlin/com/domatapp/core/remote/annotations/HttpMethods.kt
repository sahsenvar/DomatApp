package com.domatapp.core.remote.annotations

/**
 * HTTP GET request.
 *
 * Usage:
 * ```kotlin
 * @GET("users/{id}")
 * suspend fun getUser(@Path id: String): User
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class GET(val path: String)

/**
 * HTTP POST request.
 *
 * Usage:
 * ```kotlin
 * @POST("auth/login")
 * suspend fun login(@Body credentials: Credentials): User
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class POST(val path: String)

/**
 * HTTP PUT request.
 *
 * Usage:
 * ```kotlin
 * @PUT("users/{id}")
 * suspend fun updateUser(@Path id: String, @Body user: User): User
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class PUT(val path: String)

/**
 * HTTP PATCH request.
 *
 * Usage:
 * ```kotlin
 * @PATCH("users/{id}")
 * suspend fun patchUser(@Path id: String, @Body updates: Map<String, Any>): User
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class PATCH(val path: String)

/**
 * HTTP DELETE request.
 *
 * Usage:
 * ```kotlin
 * @DELETE("users/{id}")
 * suspend fun deleteUser(@Path id: String)
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class DELETE(val path: String)
