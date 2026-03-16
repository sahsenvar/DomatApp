package com.domatapp.core.config.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.domatapp.core.config.datastore.createDataStore
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.FirebaseRemoteConfig
import dev.gitlive.firebase.remoteconfig.remoteConfig
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.domatapp.core.config")
class CoreConfigModule {

    @Single
    @Named("auth")
    fun provideAuthDataStore(): DataStore<Preferences> = createDataStore("auth")

    @Single
    fun provideRemoteConfig(): FirebaseRemoteConfig = Firebase.remoteConfig
}
