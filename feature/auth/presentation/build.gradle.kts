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
    // :core:design, the lifecycle Compose artifacts and the Koin ViewModel/Compose artifacts as api.
    commonMainImplementation(projects.core.presentation)
    commonMainImplementation(projects.core.resulting)
    // Used directly by this module's sources (StateFlow in the ViewModels, and the iOS
    // suspendCancellableCoroutine that awaits Swift's Google sign-in callback).
    commonMainImplementation(libs.concurrency.coroutine.core)
    // Auth-specific: Google Sign-In via Credential Manager. Android only - the iOS `actual` of
    // requestGoogleIdToken delegates to the GoogleSignIn-iOS SDK through a Swift-registered
    // GoogleSignInPresenter, because that SDK is a Swift package the Kotlin compiler never sees.
    androidMainImplementation(libs.auth.credentials.core)
    androidMainImplementation(libs.auth.credentials.playServices)
    androidMainImplementation(libs.auth.googleId.core)
    // Generates provideXEntry() per @Screen, wired through :core:presentation's @ScreenWrapper.
    // Registered on kspCommonMainMetadata, not per target: the screens are common now, and what
    // Gezgin generates from them is platform-independent, so the processor runs once and its
    // output is added to commonMain by DiConventionPlugin.
    add("kspCommonMainMetadata", libs.navigation.gezgin.processor)
}

// The @ScreenWrapper and its @ScreenSlot markers are compiled into :core:presentation, and KSP
// cannot enumerate annotated declarations on the classpath - so this module names their package.
// Without it the entries here are generated UNWRAPPED (a KSP warning, not a build failure).
ksp {
    arg("gezgin.wrapperPackages", "com.domatapp.core.presentation.screen")
}
