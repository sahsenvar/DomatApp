plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.cmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    commonMainImplementation(projects.core.presentation)
    commonMainImplementation(projects.core.common)
    commonMainImplementation(projects.core.navigation)
    commonMainImplementation(projects.core.resource)
    commonMainImplementation(libs.kotlinx.coroutines.core)
    commonMainImplementation(libs.koin.core.viewmodel)
    androidMainImplementation(projects.core.design)
    androidMainImplementation(libs.koin.compose)
    androidMainImplementation(libs.navigation3.runtime)
    kspAndroid(projects.core.processor)
}

// Ensure kspAndroidMain runs after kspCommonMainKotlinMetadata (Koin KSP)
tasks.matching { it.name == "kspAndroidMain" }.configureEach {
    dependsOn(tasks.matching { it.name == "kspCommonMainKotlinMetadata" })
}
