plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

dependencies {
    commonMainImplementation(libs.kotlinx.coroutines.core)
}