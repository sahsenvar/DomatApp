plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.cmp.library)
    alias(libs.plugins.ksp)
}

dependencies {
    commonMainImplementation(projects.feature.home.domain)
    commonMainImplementation(projects.core.common)
    // :core:presentation re-exposes :core:domain, :core:common, :core:navigation, :core:resource,
    // :core:design, lifecycle-viewmodel and the Koin ViewModel/Compose artifacts as api.
    commonMainImplementation(projects.core.presentation)

    // Generates provideHomeEntry(), wired through :core:presentation's @ScreenWrapper.
    kspAndroid(libs.navigation.gezgin.processor)
}

// The @ScreenWrapper and its @ScreenSlot markers are compiled into :core:presentation, and KSP
// cannot enumerate annotated declarations on the classpath - so this module names their package.
// Without it the entries here are generated UNWRAPPED (a KSP warning, not a build failure).
ksp {
    arg("gezgin.wrapperPackages", "com.domatapp.core.presentation.screen")
}
