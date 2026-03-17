package com.domatapp.feature.auth.presentation.di

import com.domatapp.feature.auth.domain.di.AuthDomainModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * Koin module for Auth Presentation layer.
 * Uses KSP to generate definitions via @ComponentScan.
 */
@Module(includes = [AuthDomainModule::class])
@ComponentScan("com.domatapp.feature.auth.presentation")
class AuthPresentationModule
