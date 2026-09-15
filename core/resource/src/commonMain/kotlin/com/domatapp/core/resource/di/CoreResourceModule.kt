package com.domatapp.core.resource.di

import com.domatapp.core.resource.api.StringResourceApi
import com.domatapp.core.resource.impl.ComposeStringResourceApi
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.module.Module as KoinModule
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.domatapp.core.resource")
class CoreResourceModule {
    @Single
    fun provideStringResourceApi(): StringResourceApi = ComposeStringResourceApi()
}

/**
 * Entry point for loading this Koin module from another Gradle module.
 *
 * The Koin compiler plugin only generates the `module()` accessor inside the compilation that
 * declares the `@Module` class, so `CoreResourceModule().module()` does not resolve from `:shared`.
 * Every module exposes its own accessor instead.
 */
fun coreResourceModule(): KoinModule = CoreResourceModule().module()
