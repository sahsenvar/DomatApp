package com.domatapp.core.serialization.di

import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Koin module for core:serialization layer.
 * Scans for @Single annotated serialization implementations and provides a configured Json instance.
 */
@Module
@ComponentScan("com.domatapp.core.serialization")
class CoreSerializationModule {

    @Single
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false
    }
}
