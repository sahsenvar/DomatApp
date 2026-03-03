package com.domatapp.core.remote.rest.impl

import com.domatapp.core.remote.mapper.toRemoteError
import com.domatapp.core.remote.rest.api.RestApi
import com.domatapp.core.serialization.api.SerializationApi
import com.domatapp.core.resulting.error.DomainError
import io.ktor.client.HttpClient
import org.koin.core.annotation.Single
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.reflect.KClass

/**
 * Ktor implementation of RestApi for HTTP methods.
 */
@Single
class KtorRestApi(
    private val client: HttpClient,
    private val serializer: SerializationApi
) : RestApi {

    override suspend fun <T : Any> get(
        path: String,
        queryParams: Map<String, Any>,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): T = try {
        val response = client.get(path) {
            queryParams.forEach { (key, value) ->
                parameter(key, value)
            }
            headers.forEach { (key, value) ->
                header(key, value)
            }
        }

        handleResponse(response, responseType)
    } catch (e: Exception) {
        throw e as? DomainError ?: e.toRemoteError()
    }

    override suspend fun <T : Any> post(
        path: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): T = try {
        val response = client.post(path) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            body?.let {
                contentType(ContentType.Application.Json)
                setBody(serializer.serialize(it))
            }
        }

        handleResponse(response, responseType)
    } catch (e: Exception) {
        throw e.toRemoteError()
    }

    override suspend fun <T : Any> put(
        path: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): T = try {
        val response = client.put(path) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            body?.let {
                contentType(ContentType.Application.Json)
                setBody(serializer.serialize(it))
            }
        }

        handleResponse(response, responseType)
    } catch (e: Exception) {
        throw e.toRemoteError()
    }

    override suspend fun <T : Any> patch(
        path: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): T = try {
        val response = client.patch(path) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            body?.let {
                contentType(ContentType.Application.Json)
                setBody(serializer.serialize(it))
            }
        }

        handleResponse(response, responseType)
    } catch (e: Exception) {
        throw e.toRemoteError()
    }

    override suspend fun <T : Any> delete(
        path: String,
        queryParams: Map<String, Any>,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): T = try {
        val response = client.delete(path) {
            queryParams.forEach { (key, value) ->
                parameter(key, value)
            }
            headers.forEach { (key, value) ->
                header(key, value)
            }
        }

        handleResponse(response, responseType)
    } catch (e: Exception) {
        throw e.toRemoteError()
    }

    private suspend fun <T : Any> handleResponse(
        response: HttpResponse,
        responseType: KClass<T>
    ): T {
        // Special handling for Unit type
        if (responseType == Unit::class) {
            @Suppress("UNCHECKED_CAST")
            return Unit as T
        }

        val bodyText = response.bodyAsText()
        return serializer.deserialize(bodyText, responseType)
    }
}