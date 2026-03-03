package com.domatapp.feature.auth.domain.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * Koin module for Auth Domain layer.
 * Uses KSP to generate definitions via @ComponentScan.
 */
@Module
@ComponentScan("com.domatapp.feature.auth.domain")
class AuthDomainModule
