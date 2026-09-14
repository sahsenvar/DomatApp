plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

dependencies {
    commonMainImplementation(libs.concurrency.coroutine.core)
}