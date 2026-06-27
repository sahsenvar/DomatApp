package com.domatapp.app

import android.app.Application
import com.domatapp.core.config.datastore.DataStoreContextHolder
import com.domatapp.shared.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class DomatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DataStoreContextHolder.context = applicationContext
        initKoin {
            androidLogger(Level.ERROR)
            androidContext(this@DomatApplication)
        }
    }
}
