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

    // KMapper -> @MapTo codegen. The compiler registration is necessarily per-module;
    // the runtime and annotations come through :core:data.
    // KtorfitX registers its own processor itself, via its Gradle plugin.
    add("kspCommonMainMetadata", libs.mapping.kmapper.compiler)

    // KspPreferences -> @Preferences codegen, registered per target rather than on
    // kspCommonMainMetadata. It has to be: for `expect object AuthConfigSourceConstructor` it emits
    // the matching `actual object` (plus a target-specific AuthConfigSourceImpl), and an `actual`
    // declaration cannot be generated into the common metadata compilation.
    //
    // These per-target tasks also consume the commonMain srcDir that DiConventionPlugin points at
    // build/generated/ksp/metadata/commonMain/kotlin, so they need to run after
    // kspCommonMainKotlinMetadata. Unlike :feature:auth:presentation, this module does not declare
    // that edge by hand: the KtorfitX plugin already applies dependsOn("kspCommonMainKotlinMetadata")
    // to every KspAATask in the module.
    add("kspAndroid", libs.persistence.kspPreferences.compiler)
    add("kspIosArm64", libs.persistence.kspPreferences.compiler)
    add("kspIosSimulatorArm64", libs.persistence.kspPreferences.compiler)
}
