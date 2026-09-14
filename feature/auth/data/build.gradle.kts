plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktorfitx)
}

// KtorfitX owns the KSP wiring for the REST Sources: its plugin adds multiplatform-annotation and
// multiplatform-core to commonMain, registers multiplatform-ksp on every ksp* configuration, and
// orders the per-target KSP tasks after kspCommonMainKotlinMetadata. Unlike Ktorfit's plugin it
// does all of this inside afterEvaluate, so the order of the plugins {} block does not matter.
ktorfitx {
    // WebSocket support is the reason for choosing KtorfitX over Ktorfit. No feature uses @WebSocket
    // yet; this only puts the capability in place.
    websockets {
        enabled = true
    }
}

dependencies {
    commonMainImplementation(projects.feature.auth.domain)
    // :core:data re-exposes :core:domain, :core:resulting, :core:remote, :core:config and
    // :core:local as api, plus the KMapper runtime - the set every feature data module needs.
    commonMainImplementation(projects.core.data)
    // Declared explicitly because this module's own sources use them directly (@Serializable on
    // the DTOs, Flow in the repositories) rather than relying on a transitive api chain.
    commonMainImplementation(libs.concurrency.coroutine.core)
    commonMainImplementation(libs.serialization.kxSerialization.json)
    // Required, not optional: the KtorfitX plugin calls checkDependency("io.ktor", ...) against
    // this module's own declared dependencies and fails the build if they are missing, or if their
    // version is not exactly the Ktor version KtorfitX was built against.
    commonMainImplementation(libs.network.ktorClient.core)
    commonMainImplementation(libs.network.ktorClient.websockets)

    // core:processor -> @ConfigSource codegen. KtorfitX registers its own processor itself.
    add("kspCommonMainMetadata", projects.core.processor)
    // KMapper -> @MapTo codegen. The compiler registration is necessarily per-module;
    // the runtime and annotations come through :core:data.
    add("kspCommonMainMetadata", libs.mapping.kmapper.compiler)
}
