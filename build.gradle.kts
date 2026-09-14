import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

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
    alias(libs.plugins.mokoResources) apply false
    alias(libs.plugins.detekt) apply false
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
