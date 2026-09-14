plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.cmp.library)
}

dependencies {
    // Exposed as `api` on purpose. Every `feature:{name}:presentation` module needs this same set,
    // so depending on `:core:presentation` is enough - no feature has to re-declare them.
    // `:core:navigation` brings the Navigation 3 runtime with it on Android.
    commonMainApi(projects.core.domain)
    commonMainApi(projects.core.common)
    commonMainApi(projects.core.navigation)
    commonMainApi(projects.core.resource)
    commonMainApi(libs.core.lifecycle.viewmodel)
    commonMainApi(libs.di.koin.coreViewmodel)

    // Design system (theme tokens + core:resource R) used by the androidMain components
    androidMainApi(projects.core.design)
    androidMainApi(libs.di.koin.compose)

    commonMainImplementation(libs.concurrency.coroutine.core)
}
