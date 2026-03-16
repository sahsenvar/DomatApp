package com.domatapp.shared.di

import com.domatapp.core.config.di.CoreConfigModule
import com.domatapp.core.remote.di.CoreRemoteModule
import com.domatapp.core.resource.di.CoreResourceModule
import com.domatapp.core.serialization.di.CoreSerializationModule
import com.domatapp.feature.auth.data.di.AuthDataModule
import com.domatapp.feature.auth.domain.di.AuthDomainModule
import com.domatapp.feature.auth.presentation.di.AuthPresentationModule
import com.domatapp.shared.database.AppDatabase
import com.domatapp.shared.database.getDatabaseBuilder
import com.domatapp.shared.database.getRoomDatabase
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.ksp.generated.module

private val databaseModule = module {
    single<AppDatabase> { getRoomDatabase(getDatabaseBuilder()) }
    single { get<AppDatabase>().authLocalDataSource() }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication {
    return startKoin {
        appDeclaration()
        modules(
            CoreSerializationModule().module,
            CoreRemoteModule().module,
            CoreConfigModule().module,
            CoreResourceModule().module,
            databaseModule,
            AuthDomainModule().module,
            AuthDataModule().module,
            AuthPresentationModule().module,
        )
    }
}
