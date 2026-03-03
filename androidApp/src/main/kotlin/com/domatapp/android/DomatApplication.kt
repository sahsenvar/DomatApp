package com.domatapp.android

import android.app.Application
import com.domatapp.android.di.AndroidAppModule
import com.domatapp.core.remote.di.CoreRemoteModule
import com.domatapp.core.serialization.di.CoreSerializationModule
import com.domatapp.feature.auth.data.di.AuthDataModule
import com.domatapp.feature.auth.domain.di.AuthDomainModule
import com.domatapp.feature.auth.presentation.di.AuthPresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.ksp.generated.*

/**
 * Android Application class for DomatApp.
 * Initializes Koin dependency injection using KSP-generated modules.
 */
class DomatApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@DomatApplication)

            // Load all KSP-generated modules
            modules(
                AndroidAppModule().module,
                CoreSerializationModule().module,
                CoreRemoteModule().module,
                AuthDomainModule().module,
                AuthDataModule().module,
                AuthPresentationModule().module,
            )
        }
    }
}
