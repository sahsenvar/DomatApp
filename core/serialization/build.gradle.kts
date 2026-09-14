plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    commonMainApi(projects.core.resulting)
    commonMainApi(libs.kotlinx.serialization.json)
    commonMainImplementation(libs.kotlinx.coroutines.core)
}
