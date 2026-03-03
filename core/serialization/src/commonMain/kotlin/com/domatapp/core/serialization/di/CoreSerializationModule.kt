package com.domatapp.core.serialization.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * Koin module for core:serialization layer.
 * Scans for @Single annotated serialization implementations.
 */
@Module
@ComponentScan("com.domatapp.core.serialization")
class CoreSerializationModule
