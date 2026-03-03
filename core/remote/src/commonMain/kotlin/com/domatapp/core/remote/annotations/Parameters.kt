package com.domatapp.core.remote.annotations

/**
 * Marks a parameter as the request body.
 * Will be serialized and sent in the request body.
 *
 * Usage:
 * ```kotlin
 * @POST("users")
 * suspend fun createUser(@Body user: User): User
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Body

/**
 * Marks a parameter as a query parameter.
 *
 * Usage:
 * ```kotlin
 * @GET("users")
 * suspend fun getUsers(@Query("page") page: Int): List<User>
 *
 * // Custom query param name
 * @GET("search")
 * suspend fun search(@Query("q") query: String): SearchResults
 * ```
 *
 * If name is empty, parameter name will be used.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Query(val name: String = "")

/**
 * Marks a parameter as a request header.
 *
 * Usage:
 * ```kotlin
 * @GET("profile")
 * suspend fun getProfile(@Header("Authorization") token: String): Profile
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Header(val name: String)

/**
 * Marks a parameter as a map of headers.
 * All entries in the map will be added as request headers.
 *
 * Usage:
 * ```kotlin
 * @GET("data")
 * suspend fun getData(@HeaderMap headers: Map<String, String>): Data
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class HeaderMap

/**
 * Marks a parameter as a path parameter.
 * Will replace {name} placeholder in the path.
 *
 * Usage:
 * ```kotlin
 * @GET("users/{id}")
 * suspend fun getUser(@Path id: String): User
 *
 * // Custom path param name
 * @GET("posts/{postId}/comments/{commentId}")
 * suspend fun getComment(
 *     @Path("postId") post: String,
 *     @Path("commentId") comment: String
 * ): Comment
 * ```
 *
 * If name is empty, parameter name will be used.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Path(val name: String = "")
