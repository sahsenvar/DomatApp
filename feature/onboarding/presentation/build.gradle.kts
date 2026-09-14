plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.cmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    // :core:presentation re-exposes :core:domain, :core:common, :core:navigation, :core:resource,
    // :core:design, lifecycle-viewmodel and the Koin ViewModel/Compose artifacts as api.
    commonMainImplementation(projects.core.presentation)
    // Used directly by this module's sources (StateFlow in the ViewModels).
    commonMainImplementation(libs.concurrency.coroutine.core)
    kspAndroid(projects.core.processor)
}

// DiConventionPlugin registers build/generated/ksp/metadata/commonMain/kotlin as a commonMain
// srcDir, which makes kspAndroidMain an implicit consumer of kspCommonMainKotlinMetadata's
// output. Gradle needs that edge declared even though nothing generates into it today.
tasks.matching { it.name == "kspAndroidMain" }.configureEach {
    dependsOn(tasks.matching { it.name == "kspCommonMainKotlinMetadata" })
}
