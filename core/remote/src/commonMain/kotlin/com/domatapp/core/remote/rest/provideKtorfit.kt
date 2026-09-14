package com.domatapp.core.remote.rest

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import org.koin.core.annotation.Single

/**
 * Provides the shared [Ktorfit] instance used to create REST Source implementations.
 *
 * It reuses the project-wide [HttpClient] (headers, ContentNegotiation, logging and the
 * RemoteError mapping in [provideHttpClient]) so every Ktorfit call goes through exactly the
 * same pipeline as before.
 *
 * Note: Ktorfit requires [Ktorfit.Builder.baseUrl] to end with `/`, and Source paths
 * must therefore be declared WITHOUT a leading slash (e.g. `auth/v1/token`).
 */
@Single
fun provideKtorfit(httpClient: HttpClient): Ktorfit = Ktorfit.Builder()
    .httpClient(httpClient)
    .baseUrl("https://$supabaseHost/")
    .build()
