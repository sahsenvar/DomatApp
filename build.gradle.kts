import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import java.util.concurrent.TimeUnit

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.roborazzi) apply false
}

// Applied here rather than per-module: static analysis is a cross-cutting, repo-wide concern, not
// something each module's build.gradle.kts should opt into individually. `build-logic`'s own
// modules are a separate included build, not a subproject of this one, so they're out of scope for
// now - CI only runs `detekt` at the root, which walks every subproject task.
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        parallel = true
    }

    tasks.withType<Detekt>().configureEach {
        // Detekt's own default `source` is src/main and src/test only - it has no idea about KMP
        // source sets (commonMain, androidMain, iosMain, ...). Point it at the whole module and
        // filter to Kotlin files instead of enumerating every possible source set by hand.
        setSource(projectDir)
        include("**/*.kt")
        exclude("**/build/**", "**/resources/**")
    }
}

// Gezgin is pinned to 0.3.0-SNAPSHOT, and Gradle caches a changing module's resolution for 24 hours
// by default. CI restores ~/.gradle from an earlier run, so a freshly published snapshot (for
// example one carrying a processor fix) would not be picked up until that window expires. Gezgin is the
// only changing module in the build, so re-checking on every resolution costs one metadata request.
subprojects {
    configurations.configureEach {
        resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
    }
}
