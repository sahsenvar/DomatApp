plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.cmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    commonMainImplementation(projects.feature.auth.domain)
    // :core:presentation re-exposes :core:domain, :core:common, :core:navigation, :core:resource,
    // :core:design, lifecycle-viewmodel and the Koin ViewModel/Compose artifacts as api.
    commonMainImplementation(projects.core.presentation)
    commonMainImplementation(projects.core.resulting)
    // Used directly by this module's sources (StateFlow in the ViewModels).
    commonMainImplementation(libs.concurrency.coroutine.core)
    // Auth-specific: Google Sign-In via Credential Manager.
    androidMainImplementation(libs.auth.credentials.core)
    androidMainImplementation(libs.auth.credentials.playServices)
    androidMainImplementation(libs.auth.googleId.core)
    // Generates provideXEntry() per @Screen, wired through :core:presentation's
    // @ScreenWrapper. Android-only: gezgin-core has no iOS klib and these screens are
    // androidMain anyway.
    kspAndroid(libs.navigation.gezgin.processor)
}

// DiConventionPlugin registers build/generated/ksp/metadata/commonMain/kotlin as a commonMain
// srcDir, which makes kspAndroidMain an implicit consumer of kspCommonMainKotlinMetadata's
// output. Gradle needs that edge declared even though nothing generates into it today.
tasks.matching { it.name == "kspAndroidMain" }.configureEach {
    dependsOn(tasks.matching { it.name == "kspCommonMainKotlinMetadata" })
}

// The @ScreenWrapper and its @ScreenSlot markers are compiled into :core:presentation, and KSP
// cannot enumerate annotated declarations on the classpath - so this module names their package.
// Without it the entries here are generated UNWRAPPED (a KSP warning, not a build failure).
ksp {
    arg("gezgin.wrapperPackages", "com.domatapp.core.presentation.screen")
}
