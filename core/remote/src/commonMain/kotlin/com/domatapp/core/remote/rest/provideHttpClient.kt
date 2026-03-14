package com.domatapp.core.remote.rest

import com.domatapp.core.remote.mapper.toRemoteError
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.seconds

@Single
fun provideHttpClient(json: Json): HttpClient = HttpClient {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.INFO
    }

    install(WebSockets) {
        pingInterval = 20.seconds
    }

    install(ContentNegotiation) {
        json(json)
    }

    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, _ ->
            throw exception.toRemoteError()
        }
    }
}