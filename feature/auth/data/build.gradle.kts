plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    // Order matters: the Ktorfit plugin does all of its KSP wiring inside an
    // `if (extensions.findByName("ksp") != null)` branch, evaluated when it is applied. Applying
    // it before the KSP plugin silently skips that branch - the processor never registers and the
    // generated createXxxDataSource() extensions just do not exist. Keep ksp above ktorfit.
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktorfit)
}

// The Ktorfit Gradle plugin owns the KSP wiring for the REST DataSources: it registers
// ktorfit-ksp on kspCommonMainMetadata (and on the per-target ksp configurations, where the
// processor deliberately generates nothing for commonMain-declared interfaces), passes its
// Ktorfit_* KSP options, and orders the compile tasks after kspCommonMainKotlinMetadata.
ktorfit {
    // Disable the Ktorfit *compiler* plugin. It exists only to rewrite the reified
    // Ktorfit.create<T>() call, and that function is @Deprecated in 2.7.5 ("the plan is to get
    // rid of the plugin") - with kotlin.compiler.allWarningsAsErrors=true it could not be used
    // here anyway. AuthDataModule calls the KSP-generated ktorfit.createXxxDataSource()
    // extensions instead, so nothing needs the compiler plugin, and skipping it means no
    // Kotlin-version-coupled compiler artifact is loaded into the build at all.
    compilerPluginVersion.set("-")
}

dependencies {
    commonMainImplementation(projects.feature.auth.domain)
    // :core:data re-exposes :core:domain, :core:resulting, :core:remote, :core:config and
    // :core:local as api - the set every feature data module needs.
    commonMainImplementation(projects.core.data)
    commonMainImplementation(projects.core.mapping)
    // Declared explicitly because this module's own sources use them directly (@Serializable on
    // the DTOs, Flow in the repositories) rather than relying on a transitive api chain.
    commonMainImplementation(libs.concurrency.coroutine.core)
    commonMainImplementation(libs.serialization.kxSerialization.json)

    commonMainImplementation(libs.network.ktorfit.libLight)
    commonMainImplementation(libs.network.ktorfit.annotations)

    add("kspCommonMainMetadata", projects.core.processor)

    // The Ktorfit Gradle plugin 2.7.5 hardcodes KTORFIT_KSP_PLUGIN_VERSION = "2.7.3", so on its
    // own it would pair the 2.7.5 runtime with the 2.7.3 processor. Declaring the catalog
    // version here lets Gradle's newest-wins conflict resolution keep both at 2.7.5.
    add("kspCommonMainMetadata", libs.network.ktorfit.ksp)
}
