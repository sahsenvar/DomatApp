plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
}

dependencies {
    commonMainImplementation(projects.core.domain)
    commonMainApi(projects.core.resulting)
    commonMainApi(projects.core.common)
    commonMainImplementation(libs.serialization.kxSerialization.json)
}
