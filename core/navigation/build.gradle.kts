plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    commonMainImplementation(libs.serialization.kxSerialization.json)

    // The Navigation 3 runtime belongs with the navigation module rather than being re-declared by
    // every presentation module. Android-only: Navigation 3 is an androidx artifact.
    androidMainApi(libs.navigation.nav3.runtime)
}
