package com.domatapp.core.resource.di

import com.domatapp.core.resource.api.StringResourceApi
import com.domatapp.core.resource.impl.createStringResourceApi
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.domatapp.core.resource")
class CoreResourceModule {
    @Single
    fun provideStringResourceApi(): StringResourceApi = createStringResourceApi()
}
