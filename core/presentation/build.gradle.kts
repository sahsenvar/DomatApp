plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.cmp.library)
}

dependencies {
    // Exposed as `api` on purpose. Every `feature:{name}:presentation` module needs this same set,
    // so depending on `:core:presentation` is enough - no feature has to re-declare them.
    // `:core:navigation` brings Gezgin (and with it the Navigation 3 runtime) on Android,
    // plus the generated route navigators every effect handler names.
    commonMainApi(projects.core.domain)
    commonMainApi(projects.core.common)
    commonMainApi(projects.core.navigation)
    commonMainApi(projects.core.resource)
    commonMainApi(libs.core.lifecycle.viewmodel)
    commonMainApi(libs.di.koin.coreViewmodel)

    // Design system (theme tokens + core:resource R) used by the androidMain components
    androidMainApi(projects.core.design)
    androidMainApi(libs.di.koin.compose)
    // `koinViewModel()` for the @ViewModelOf providers. Unlike `koinInject()` it resolves against
    // LocalViewModelStoreOwner, which under GezginDisplay is the per-NavEntry ViewModelStore - so
    // a screen's ViewModel dies with its back-stack entry instead of with the Activity.
    androidMainApi(libs.di.koin.composeViewmodel)
    // DomatScreenRoot collects state with collectAsStateWithLifecycle.
    androidMainApi(libs.core.lifecycle.runtimeCompose)

    commonMainImplementation(libs.concurrency.coroutine.core)
}
