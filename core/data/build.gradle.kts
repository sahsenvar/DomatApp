plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

dependencies {
    // Exposed as `api` on purpose. Every `feature:{name}:data` module needs this same set by
    // convention, so depending on `:core:data` is enough - no feature has to re-declare them, and
    // the layering rule lives in one place instead of being copy-pasted per feature.
    commonMainApi(projects.core.domain)
    commonMainApi(projects.core.resulting)
    commonMainApi(projects.core.remote)
    commonMainApi(projects.core.config)
    commonMainApi(projects.core.local)
    // Every feature data module maps DTOs to domain models, so the KMapper runtime and
    // annotations belong here too. Only the KSP compiler stays per-module.
    commonMainApi(libs.mapping.kmapper.core)
    commonMainApi(libs.mapping.kmapper.annotations)

    commonMainImplementation(libs.concurrency.coroutine.core)
}
