plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    commonMainApi(projects.core.resulting)
    commonMainImplementation(libs.concurrency.coroutine.core)
    commonMainApi(libs.persistence.dataStore.core)
    commonMainApi(libs.persistence.dataStore.preferences)
    commonMainImplementation(libs.backend.firebase.config)
    commonMainImplementation(libs.serialization.kxSerialization.json)
    androidMainImplementation(project.dependencies.platform(libs.backend.firebase.bom))
}
