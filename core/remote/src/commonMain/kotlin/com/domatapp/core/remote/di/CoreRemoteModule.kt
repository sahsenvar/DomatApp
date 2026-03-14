package com.domatapp.core.remote.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * Koin module for core:remote layer.
 * Provides HttpClient and discovers concrete clients (KtorSocketClient, etc.) via @ComponentScan.
 */
@Module
@ComponentScan("com.domatapp.core.remote")
class CoreRemoteModule
