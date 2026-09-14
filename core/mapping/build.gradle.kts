plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

dependencies {
    commonMainImplementation(libs.concurrency.coroutine.core)
    commonMainImplementation(libs.datetime.kxDateTime.core)
    commonMainImplementation(libs.collections.kxCollections.immutable)
    commonMainApi(projects.core.resulting)
}
