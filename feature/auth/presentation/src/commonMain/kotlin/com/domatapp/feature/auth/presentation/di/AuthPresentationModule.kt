package com.domatapp.feature.auth.presentation.di

import com.domatapp.feature.auth.domain.di.AuthDomainModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.module.Module as KoinModule

/**
 * Koin module for Auth Presentation layer.
 * Uses KSP to generate definitions via @ComponentScan.
 */
@Module(includes = [AuthDomainModule::class])
@ComponentScan("com.domatapp.feature.auth.presentation")
class AuthPresentationModule

/**
 * Entry point for loading this Koin module from another Gradle module.
 *
 * The Koin compiler plugin only generates the `module()` accessor inside the compilation that
 * declares the `@Module` class, so `AuthPresentationModule().module()` does not resolve from `:shared`.
 * Every module exposes its own accessor instead.
 */
fun authPresentationModule(): KoinModule = AuthPresentationModule().module()
