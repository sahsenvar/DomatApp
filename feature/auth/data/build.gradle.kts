plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
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
    // core:processor -> @RemoteDataSource / @ConfigDataSource codegen
    add("kspCommonMainMetadata", projects.core.processor)
    // KMapper -> @MapTo codegen. The compiler registration is necessarily per-module;
    // the runtime and annotations come through :core:data.
    add("kspCommonMainMetadata", libs.mapping.kmapper.compiler)
}
