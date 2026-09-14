package com.domatapp.shared.di

import com.domatapp.core.config.di.coreConfigModule
import com.domatapp.core.remote.di.coreRemoteModule
import com.domatapp.core.resource.di.coreResourceModule
import com.domatapp.feature.auth.data.di.authDataModule
import com.domatapp.feature.auth.domain.di.authDomainModule
import com.domatapp.feature.auth.presentation.di.authPresentationModule
import com.domatapp.feature.onboarding.presentation.di.onboardingPresentationModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication {
    return startKoin {
        appDeclaration()
        // Each Gradle module exposes its own accessor: the Koin compiler plugin generates the
        // `module()` accessor only inside the compilation that declares the @Module class, so
        // `CoreRemoteModule().module()` cannot be called from here.
        modules(
            coreRemoteModule(),
            coreConfigModule(),
            coreResourceModule(),
            authDomainModule(),
            authDataModule(),
            authPresentationModule(),
            onboardingPresentationModule(),
        )
    }
}
