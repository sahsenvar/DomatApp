plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.cmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
}

dependencies {
    commonMainImplementation(projects.feature.checkout.domain)
    // :core:presentation re-exposes :core:domain, :core:common, :core:navigation, :core:resource,
    // :core:design, lifecycle-viewmodel and the Koin ViewModel/Compose artifacts as api.
    commonMainImplementation(projects.core.presentation)
    // Used directly by this module's sources (StateFlow / viewModelScope work in the ViewModel).
    commonMainImplementation(libs.concurrency.coroutine.core)
    // Generates provideXEntry() per @Screen, wired through :core:presentation's @ScreenWrapper.
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
