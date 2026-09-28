plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.test)
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
    // kspCommonMainMetadata rather than per target: the screens are common now and Gezgin's output
    // for them is platform-independent. DiConventionPlugin adds that output to commonMain and
    // orders every compile and per-target KSP task after it.
    add("kspCommonMainMetadata", libs.navigation.gezgin.processor)
}

// DomatScreenRoot is compiled into :core:presentation, and KSP cannot enumerate annotated
// declarations on the classpath - so this module names it. `wrapperDeclarations` (the wrapper's
// fully-qualified name) rather than `wrapperPackages` (its package): the latter enumerates the
// package, which returns nothing when the dependency arrives as Kotlin *metadata*, as it does in a
// kspCommonMainMetadata round. The @ScreenSlot markers need no naming - Gezgin reaches them through
// the wrapper's @FilledBy parameters.
//
// Without this the entries here are generated UNWRAPPED (a KSP warning, not a build failure).
ksp {
    arg("gezgin.wrapperDeclarations", "com.domatapp.core.presentation.screen.DomatScreenRoot")
}
