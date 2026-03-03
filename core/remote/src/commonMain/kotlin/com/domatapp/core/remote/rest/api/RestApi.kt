package com.domatapp.core.remote.rest.api

import kotlin.reflect.KClass

/**
 * REST API abstraction for HTTP methods.
 * Implementations: KtorRestApi, RetrofitRestApi, etc.
 */
interface RestApi {

    /**
     * HTTP GET request
     */
    suspend fun <T : Any> get(
        path: String,
        queryParams: Map<String, Any> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        responseType: KClass<T>
    ): T

    /**
     * HTTP POST request
     */
    suspend fun <T : Any> post(
        path: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        responseType: KClass<T>
    ): T

    /**
     * HTTP PUT request
     */
    suspend fun <T : Any> put(
        path: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        responseType: KClass<T>
    ): T

    /**
     * HTTP PATCH request
     */
    suspend fun <T : Any> patch(
        path: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        responseType: KClass<T>
    ): T

    /**
     * HTTP DELETE request
     */
    suspend fun <T : Any> delete(
        path: String,
        queryParams: Map<String, Any> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        responseType: KClass<T>
    ): T
}