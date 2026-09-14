package com.domatapp.core.remote.rest

import com.domatapp.core.common.presentation.Environment
import com.domatapp.core.remote.mapper.toRemoteError
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.addDefaultResponseValidation
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.auth.AuthScheme
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.seconds

@Single
fun provideHttpClient(json: Json): HttpClient = HttpClient {

    addDefaultResponseValidation()

    Logging {
        logger = Logger.DEFAULT
        level = LogLevel.INFO
    }

    install(ContentNegotiation) {
        json(json)
    }

    WebSockets {
        pingInterval = 20.seconds
    }

    defaultRequest {
        url(scheme = URLProtocol.HTTPS.name, host = supabaseHost)

        header("apikey", publicKey)
        if (Environment.accessToken != null)
            header(HttpHeaders.Authorization, "${AuthScheme.Bearer} ${Environment.accessToken}")
    }

    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, _ ->
            throw exception.toRemoteError()
        }
    }
}

// Buraso gizli tutulabilir.
const val supabaseHost = "uscecumnpksbrhyylgpl.supabase.co"
const val publicKey = "sb_publishable_K3hi-jspeGKaqzsEwnHmKA_sH-rzf6P"
