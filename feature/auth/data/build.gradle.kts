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
    commonMainImplementation(projects.core.mapping)
    commonMainImplementation(libs.concurrency.coroutine.core)
    commonMainImplementation(libs.serialization.kxSerialization.json)
    add("kspCommonMainMetadata", projects.core.processor)
}
