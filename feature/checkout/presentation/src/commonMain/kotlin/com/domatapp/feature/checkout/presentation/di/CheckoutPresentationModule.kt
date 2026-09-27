package com.domatapp.feature.checkout.presentation.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.module.Module as KoinModule

/** Koin module for the checkout presentation layer (definitions found via @ComponentScan). */
@Module
@ComponentScan("com.domatapp.feature.checkout.presentation")
class CheckoutPresentationModule

/**
 * Entry point for loading this Koin module from `:shared`: the Koin compiler plugin generates the
 * `module()` accessor only inside the compilation that declares the `@Module` class.
 */
fun checkoutPresentationModule(): KoinModule = CheckoutPresentationModule().module()
