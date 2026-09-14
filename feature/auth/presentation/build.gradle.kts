plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.cmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    commonMainImplementation(projects.feature.auth.domain)
    commonMainImplementation(projects.core.domain)
    commonMainImplementation(projects.core.presentation)
    commonMainImplementation(projects.core.common)
    commonMainImplementation(projects.core.resulting)
    commonMainImplementation(projects.core.navigation)
    commonMainImplementation(projects.core.resource)
    commonMainImplementation(libs.concurrency.coroutine.core)
    commonMainImplementation(libs.di.koin.coreViewmodel)
    androidMainImplementation(projects.core.design)
    androidMainImplementation(libs.di.koin.compose)
    androidMainImplementation(libs.navigation.nav3.runtime)
    androidMainImplementation(libs.auth.credentials.core)
    androidMainImplementation(libs.auth.credentials.playServices)
    androidMainImplementation(libs.auth.googleId.core)
    kspAndroid(projects.core.processor)
}

// Ensure kspAndroidMain runs after kspCommonMainKotlinMetadata (Koin KSP)
tasks.matching { it.name == "kspAndroidMain" }.configureEach {
    dependsOn(tasks.matching { it.name == "kspCommonMainKotlinMetadata" })
}
