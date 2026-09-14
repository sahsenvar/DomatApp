package com.domatapp.core.remote.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.module.Module as KoinModule

/**
 * Koin module for core:remote layer.
 * Provides HttpClient and discovers concrete clients (KtorSocketClient, etc.) via @ComponentScan.
 */
@Module
@ComponentScan("com.domatapp.core.remote")
class CoreRemoteModule

/**
 * Entry point for loading this Koin module from another Gradle module.
 *
 * The Koin compiler plugin only generates the `module()` accessor inside the compilation that
 * declares the `@Module` class, so `CoreRemoteModule().module()` does not resolve from `:shared`.
 * Every module exposes its own accessor instead.
 */
fun coreRemoteModule(): KoinModule = CoreRemoteModule().module()
