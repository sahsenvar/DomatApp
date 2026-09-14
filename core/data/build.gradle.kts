plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

dependencies {
    commonMainApi(project(":core:domain"))
    commonMainImplementation(libs.kotlinx.coroutines.core)
    commonMainImplementation(libs.koin.core)
}
