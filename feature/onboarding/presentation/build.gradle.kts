plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.cmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    // :core:presentation re-exposes :core:domain, :core:common, :core:navigation, :core:resource,
    // :core:design, the lifecycle Compose artifacts and the Koin ViewModel/Compose artifacts as api.
    commonMainImplementation(projects.core.presentation)
    // Used directly by this module's sources (StateFlow in the ViewModels).
    commonMainImplementation(libs.concurrency.coroutine.core)
    // Generates provideXEntry() per @Screen, wired through :core:presentation's @ScreenWrapper.
    // kspCommonMainMetadata rather than per target: the screens are common now and Gezgin's output
    // for them is platform-independent. DiConventionPlugin adds that output to commonMain and
    // orders every compile and per-target KSP task after it.
    add("kspCommonMainMetadata", libs.navigation.gezgin.processor)
}

// The @ScreenWrapper and its @ScreenSlot markers are compiled into :core:presentation, and KSP
// cannot enumerate annotated declarations on the classpath - so this module names their package.
// Without it the entries here are generated UNWRAPPED (a KSP warning, not a build failure).
ksp {
    arg("gezgin.wrapperPackages", "com.domatapp.core.presentation.screen")
}
