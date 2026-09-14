plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.cmp.library)
}

dependencies {
    commonMainImplementation(projects.core.domain)
    commonMainImplementation(projects.core.common)
    commonMainImplementation(projects.core.navigation)
    commonMainImplementation(libs.concurrency.coroutine.core)
    commonMainApi(libs.core.lifecycle.viewmodel)
    // Design system (theme tokens + core:resource R) used by the androidMain components
    androidMainImplementation(projects.core.design)
}
