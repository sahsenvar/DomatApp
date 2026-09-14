plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    commonMainApi(projects.core.resulting)
    commonMainApi(libs.serialization.kxSerialization.json)
    commonMainImplementation(libs.concurrency.coroutine.core)
}
