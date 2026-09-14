package com.domatapp.feature.auth.domain.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.module.Module as KoinModule

/**
 * Koin module for Auth Domain layer.
 * Definitions are discovered via @ComponentScan by the Koin compiler plugin.
 */
@Module
@ComponentScan("com.domatapp.feature.auth.domain")
class AuthDomainModule

/**
 * Entry point for loading this Koin module from another Gradle module.
 *
 * The Koin compiler plugin only generates the `module()` accessor inside the compilation that
 * declares the `@Module` class, so `AuthDomainModule().module()` does not resolve from `:shared`.
 * Every module exposes its own accessor instead.
 */
fun authDomainModule(): KoinModule = AuthDomainModule().module()
