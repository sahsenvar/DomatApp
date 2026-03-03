package com.domatapp.android.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * Koin module for androidApp.
 * ComponentScan will discover all Koin modules from dependencies.
 */
@Module
@ComponentScan("com.domatapp")
class AndroidAppModule
