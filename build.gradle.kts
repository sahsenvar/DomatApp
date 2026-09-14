import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

// This is the very first real Gradle invocation this project has ever run (CI was
// `echo "build ok"` until this PR) - it immediately surfaces a pre-existing, previously invisible
// conflict on the root project's own plugin/buildscript classpath, nothing to do with anything
// declared below: build-logic:convention applies `kotlin-dsl`, whose embedded-Kotlin tooling adds a
// `strictly 13.0` constraint on org.jetbrains:annotations to protect Gradle's own bundled Kotlin
// runtime; AGP 9.4.0 (a build-logic:convention dependency, needed at runtime for
// `pluginManager.apply("com.android.kotlin.multiplatform.library")` to resolve) transitively pulls
// org.jetbrains:annotations:23.0.0 via ddmlib/repository/layoutlib-api. Both land on the same
// composite build classpath and Gradle refuses to pick one on its own.
// `force` is documented to win over `strictly` for exactly this kind of unresolvable disagreement,
// and org.jetbrains:annotations has never had a breaking release - it only adds annotations.
buildscript {
    configurations.classpath {
        resolutionStrategy {
            force("org.jetbrains:annotations:23.0.0")
        }
    }
}

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
