plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

dependencies {
    commonMainApi(project(":core:domain"))
    commonMainImplementation(libs.concurrency.coroutine.core)
    commonMainImplementation(libs.di.koin.core)
}
