plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.cmp.library)
}

dependencies {
    // Exposed as `api` on purpose. Every `feature:{name}:presentation` module needs this same set,
    // so depending on `:core:presentation` is enough - no feature has to re-declare them.
    // `:core:navigation` brings Gezgin (and with it the Navigation 3 runtime) on both platforms,
    // plus the generated route navigators every effect handler names.
    //
    // All `commonMain` now: with Gezgin publishing iOS klibs, the components, the `@ScreenWrapper`
    // and the screens that use them are shared rather than androidMain-only.
    commonMainApi(projects.core.domain)
    commonMainApi(projects.core.common)
    commonMainApi(projects.core.navigation)
    commonMainApi(projects.core.resource)
    // Design system (theme tokens, DomatColors) used by the components here and by every screen.
    commonMainApi(projects.core.design)
    commonMainApi(libs.core.lifecycle.viewmodel)
    // `viewModel { }` for the @ViewModelOf providers of screens with no injected collaborators.
    //
    // Declared here rather than inherited: gezgin-core deliberately keeps the JetBrains Navigation 3
    // and lifecycle artifacts out of its *common* metadata POM (they are `compileOnly` on its shared
    // non-Android source set and `api` only from its leaf ones), so they do not reach a consumer's
    // commonMain through it. Gezgin's own iOS sample declares the same two coordinates for the same
    // reason.
    commonMainApi(libs.core.lifecycle.viewmodelCompose)
    // DomatScreenRoot collects state with collectAsStateWithLifecycle.
    commonMainApi(libs.core.lifecycle.runtimeCompose)
    commonMainApi(libs.di.koin.coreViewmodel)
    commonMainApi(libs.di.koin.compose)
    // `koinViewModel()` for the @ViewModelOf providers. Unlike `koinInject()` it resolves against
    // LocalViewModelStoreOwner, which under GezginDisplay is the per-NavEntry ViewModelStore - so
    // a screen's ViewModel dies with its back-stack entry instead of with the host.
    commonMainApi(libs.di.koin.composeViewmodel)

    commonMainImplementation(libs.concurrency.coroutine.core)
}
