package com.domatapp.core.local.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.domatapp.core.local.database.AppDatabase
import com.domatapp.core.local.database.dao.AuthSessionDao
import com.domatapp.core.local.database.getDatabaseBuilder
import com.domatapp.core.local.database.getRoomDatabase
import com.domatapp.core.local.factory.createDataStore
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
class CoreLocalModule {

    @Single
    fun provideDatabase(): AppDatabase = getRoomDatabase(getDatabaseBuilder())

    @Single
    fun provideAuthSessionDao(database: AppDatabase): AuthSessionDao = database.authSessionDao()

    @Single
    @Named("authDataStore")
    fun provideAuthDataStore(): DataStore<Preferences> = createDataStore("auth")
}
