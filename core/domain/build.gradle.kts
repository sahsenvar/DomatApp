plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

dependencies {
    commonMainApi(libs.kotlinx.coroutines.core)
    commonMainImplementation(libs.koin.core)
}
