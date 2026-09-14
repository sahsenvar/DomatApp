package com.domatapp.feature.onboarding.presentation.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.module.Module as KoinModule

@Module
@ComponentScan("com.domatapp.feature.onboarding.presentation")
class OnboardingPresentationModule

/**
 * Entry point for loading this Koin module from another Gradle module.
 *
 * The Koin compiler plugin only generates the `module()` accessor inside the compilation that
 * declares the `@Module` class, so `OnboardingPresentationModule().module()` does not resolve from `:shared`.
 * Every module exposes its own accessor instead.
 */
fun onboardingPresentationModule(): KoinModule = OnboardingPresentationModule().module()
