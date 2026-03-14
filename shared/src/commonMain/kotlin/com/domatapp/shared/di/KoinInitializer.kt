package com.domatapp.shared.di

import com.domatapp.core.local.di.CoreLocalModule
import com.domatapp.core.remote.di.CoreRemoteModule
import com.domatapp.core.resource.di.CoreResourceModule
import com.domatapp.core.serialization.di.CoreSerializationModule
import com.domatapp.feature.auth.data.di.AuthDataModule
import com.domatapp.feature.auth.domain.di.AuthDomainModule
import com.domatapp.feature.auth.presentation.di.AuthPresentationModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.ksp.generated.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication {
    return startKoin {
        appDeclaration()
        modules(
            CoreSerializationModule().module,
            CoreRemoteModule().module,
            CoreLocalModule().module,
            CoreResourceModule().module,
            AuthDomainModule().module,
            AuthDataModule().module,
            AuthPresentationModule().module,
        )
    }
}