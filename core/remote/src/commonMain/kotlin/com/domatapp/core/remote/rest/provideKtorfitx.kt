package com.domatapp.core.remote.rest

import cn.ktorfitx.multiplatform.core.Ktorfitx
import cn.ktorfitx.multiplatform.core.config.KtorfitxConfig
import cn.ktorfitx.multiplatform.core.ktorfitx
import com.domatapp.core.common.presentation.Environment
import com.domatapp.core.remote.mapper.toRemoteError
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.addDefaultResponseValidation
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.auth.AuthScheme
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

/**
 * Selects the platform Ktor engine and applies [configure] to it.
 *
 * KtorfitX cannot be handed an already-built [HttpClient] — [KtorfitxConfig] builds its own — and
 * its `httpClient(engineFactory) { }` overload is the only way to supply configuration, so the
 * engine has to be named explicitly instead of being picked up by Ktor's service loader.
 */
internal expect fun KtorfitxConfig.platformHttpClient(configure: HttpClientConfig<*>.() -> Unit)

/**
 * The single [Ktorfitx] instance. Its embedded [HttpClient] carries the whole request pipeline:
 * Supabase headers, ContentNegotiation, logging, and the RemoteError mapping.
 *
 * `baseUrl` must end with `/`, and @Api / @GET / @POST paths must therefore be declared WITHOUT a
 * leading slash (e.g. `auth/v1/token`).
 */
@Single
fun provideKtorfitx(json: Json): Ktorfitx = ktorfitx {
    baseUrl = "https://$supabaseHost/"

    platformHttpClient {
        addDefaultResponseValidation()

        Logging {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        install(ContentNegotiation) {
            json(json)
        }

        // The host is already set by KtorfitxConfig.build() from `baseUrl`; this only adds headers.
        defaultRequest {
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
}

/**
 * The app-wide [HttpClient] is the one KtorfitX built, so anything talking to the network directly
 * shares the exact same pipeline as the generated @Api implementations.
 */
@Single
fun provideHttpClient(ktorfitx: Ktorfitx): HttpClient = ktorfitx.config.httpClient

// Buraso gizli tutulabilir.
const val supabaseHost = "uscecumnpksbrhyylgpl.supabase.co"
const val publicKey = "sb_publishable_K3hi-jspeGKaqzsEwnHmKA_sH-rzf6P"
