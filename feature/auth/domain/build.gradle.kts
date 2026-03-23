plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
}

dependencies {
    commonMainImplementation(projects.core.domain)
    commonMainApi(projects.core.resulting)
    commonMainApi(projects.core.common)
    commonMainImplementation(libs.kotlinx.serialization.json)
}
