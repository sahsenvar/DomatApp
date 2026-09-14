plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    commonMainImplementation(libs.serialization.kxSerialization.json)
}
