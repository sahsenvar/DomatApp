plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    commonMainApi(projects.core.resulting)
    commonMainImplementation(projects.core.serialization)
    commonMainImplementation(libs.concurrency.coroutine.core)
    commonMainApi(libs.storage.dataStore.core)
    commonMainApi(libs.storage.dataStore.preferences)
    commonMainImplementation(libs.backend.firebase.config)
    commonMainImplementation(libs.serialization.kxSerialization.json)
    androidMainImplementation(project.dependencies.platform(libs.backend.firebase.bom))
}
