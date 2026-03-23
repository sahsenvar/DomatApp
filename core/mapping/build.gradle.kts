plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

dependencies {
    commonMainImplementation(libs.kotlinx.coroutines.core)
    commonMainImplementation(libs.kotlinx.datetime)
    commonMainImplementation(libs.kotlinx.collections.immutable)
    commonMainApi(projects.core.resulting)
}
