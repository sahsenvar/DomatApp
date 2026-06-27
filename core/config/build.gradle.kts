plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    commonMainApi(projects.core.resulting)
    commonMainImplementation(projects.core.serialization)
    commonMainImplementation(libs.kotlinx.coroutines.core)
    commonMainApi(libs.kvStore.datastore)
    commonMainApi(libs.kvStore.datastore.preferences)
    commonMainImplementation(libs.firebase.config)
    commonMainImplementation(libs.kotlinx.serialization.json)
    androidMainImplementation(project.dependencies.platform(libs.firebase.bom))
}
