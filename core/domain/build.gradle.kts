plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

dependencies {
    commonMainApi(libs.concurrency.coroutine.core)
    commonMainImplementation(libs.di.koin.core)
}
