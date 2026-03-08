package com.domatapp.core.local.di

import app.cash.sqldelight.db.SqlDriver
import com.domatapp.core.local.api.KeyValueApi
import com.domatapp.core.local.database.DomatAppDatabase
import com.domatapp.core.local.database.factory.createSqlDriver
import com.domatapp.core.local.factory.createDataStore
import com.domatapp.core.local.impl.DataStoreKeyValueApiImpl
import com.domatapp.core.serialization.api.SerializationApi
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
class CoreLocalModule {

    @Single
    fun provideSqlDriver(): SqlDriver = createSqlDriver("domatapp")

    @Single
    fun provideDatabase(driver: SqlDriver): DomatAppDatabase = DomatAppDatabase(driver)

    @Factory
    @Named("auth")
    fun provideAuthKeyValueApi(
        serializationApi: SerializationApi
    ): KeyValueApi = DataStoreKeyValueApiImpl(
        serializationApi = serializationApi,
        name = "auth"
    )
}
