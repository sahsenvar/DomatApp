package com.domatapp.core.remote.di

import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.seconds

/**
 * Koin module for core:remote layer.
 * Provides HttpClient and discovers concrete clients (KtorRestClient, KtorSocketClient, etc.) via @ComponentScan.
 */
@Module
@ComponentScan("com.domatapp.core.remote")
class CoreRemoteModule {

    @Single
    fun provideHttpClient(): HttpClient {
        return HttpClient {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            install(WebSockets) {
                pingInterval = 20.seconds
            }
        }
    }
}
