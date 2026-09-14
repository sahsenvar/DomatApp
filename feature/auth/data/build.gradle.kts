plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    commonMainImplementation(projects.feature.auth.domain)
    commonMainImplementation(projects.core.remote)
    commonMainImplementation(projects.core.config)
    commonMainImplementation(projects.core.data)
    commonMainImplementation(libs.mapping.kmapper.core)
    commonMainImplementation(libs.mapping.kmapper.annotations)
    commonMainImplementation(libs.concurrency.coroutine.core)
    commonMainImplementation(libs.serialization.kxSerialization.json)
    // core:processor -> @RemoteDataSource / @ConfigDataSource codegen
    add("kspCommonMainMetadata", projects.core.processor)
    // KMapper -> @MapTo codegen
    add("kspCommonMainMetadata", libs.mapping.kmapper.compiler)
}
