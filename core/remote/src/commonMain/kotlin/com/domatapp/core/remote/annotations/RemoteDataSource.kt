package com.domatapp.core.remote.annotations

/**
 * Marks an interface as a Remote Data Source.
 * KSP will generate an implementation that uses RemoteApi.
 *
 * Usage:
 * ```kotlin
 * @RemoteDataSource
 * interface AuthRemoteDataSource {
 *     @POST("auth/login")
 *     suspend fun login(@Body credentials: Credentials): User
 * }
 * ```
 *
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RemoteDataSource
