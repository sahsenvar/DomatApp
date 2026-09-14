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
    commonMainImplementation(libs.kmapper.core)
    commonMainImplementation(libs.kmapper.annotations)
    commonMainImplementation(libs.kotlinx.coroutines.core)
    commonMainImplementation(libs.kotlinx.serialization.json)
    // core:processor -> @RemoteDataSource / @ConfigDataSource codegen
    add("kspCommonMainMetadata", projects.core.processor)
    // KMapper -> @MapTo codegen
    add("kspCommonMainMetadata", libs.kmapper.compiler)
}
