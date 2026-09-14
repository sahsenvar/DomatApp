package com.domatapp.core.remote.di

import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.module.Module as KoinModule

/**
 * Koin module for core:remote layer.
 * Provides the shared [Json], the HttpClient and Ktorfit, and discovers @Single clients via
 * @ComponentScan.
 */
@Module
@ComponentScan("com.domatapp.core.remote")
class CoreRemoteModule {

    /**
     * The single [Json] configuration for the whole app.
     *
     * This used to live in a dedicated `:core:serialization` module behind a `SerializationApi`
     * abstraction. Nothing ever called through that abstraction, so the module was removed and the
     * one thing it really provided moved here, next to its main consumer (ContentNegotiation and
     * Ktorfit). `core:config`'s FirebaseRemoteConfigClient resolves the same instance through Koin.
     */
    @Single
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false
    }
}

/**
 * Entry point for loading this Koin module from another Gradle module.
 *
 * The Koin compiler plugin only generates the `module()` accessor inside the compilation that
 * declares the `@Module` class, so `CoreRemoteModule().module()` does not resolve from `:shared`.
 * Every module exposes its own accessor instead.
 */
fun coreRemoteModule(): KoinModule = CoreRemoteModule().module()
